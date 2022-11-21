/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { Box, Button, Grid, Typography } from "@material-ui/core";
import type { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    StepperV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { createDatasource } from "../../rest/datasources/datasources.rest";
import type { Datasource } from "../../rest/dto/datasource.interfaces";
import { createDefaultDatasource } from "../../utils/datasources/datasources.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    AppRouteRelative,
    getDataConfigurationCreateDatasetsPath,
} from "../../utils/routes/routes.util";
import { SelectedDatasource } from "../welcome-onboard-datasource-select-datasource/welcome-onboard-datasource-select-datasource.interfaces";

const STEPS = [
    {
        subPath: AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE,
        translationLabel: "onboard-datasource-select-datasource",
    },
    {
        subPath: AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS,
        translationLabel: "onboard-datasource-onboard-datasets",
    },
] as const;

export const WelcomeOnboardDatasourceWizard: FunctionComponent = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();
    const { notify } = useNotificationProviderV1();
    const navigate = useNavigate();

    const [editedDatasource, setEditedDatasource] = useState<Datasource>(
        createDefaultDatasource()
    );
    const [selectedDatasource, setSelectedDatasource] =
        useState<SelectedDatasource>(null);

    const activeStep = useMemo(() => {
        // Tries to extract the last part of the url for
        // Uses the whole url if nothing comes up
        // This is required since the first url substring ("dataset")
        // is a part of the greater url for this module
        const urlPath: string =
            pathname.split("/").filter(Boolean).pop() || pathname;

        const activeStepDefinition = STEPS.find((candidate) =>
            urlPath.includes(candidate.subPath)
        );

        if (!activeStepDefinition) {
            return "";
        }

        return activeStepDefinition.subPath;
    }, [pathname]);

    const goToDatasetPage = useCallback(
        () => navigate(getDataConfigurationCreateDatasetsPath()),
        []
    );

    const handleCreateNewDatasource = useCallback(
        (editedDatasourceProp: Datasource) =>
            createDatasource(editedDatasourceProp)
                .then((datasource: Datasource): number => {
                    notify(
                        NotificationTypeV1.Success,
                        t("message.create-success", {
                            entity: t("label.datasource"),
                        })
                    );

                    setSelectedDatasource(datasource.id);

                    return datasource.id;
                })
                .catch((error: AxiosError): void => {
                    const errMessages = getErrorMessages(error);

                    isEmpty(errMessages)
                        ? notify(
                              NotificationTypeV1.Error,
                              t("message.create-error", {
                                  entity: t("label.datasource"),
                              })
                          )
                        : errMessages.map((err) =>
                              notify(NotificationTypeV1.Error, err)
                          );
                }),
        []
    );

    const handleNextClick = useCallback(
        async (
            selectedDatasourceProp: SelectedDatasource,
            editedDatasourceProp: Datasource
        ) => {
            if (
                activeStep ===
                AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE
            ) {
                if (selectedDatasourceProp === null) {
                    notify(
                        NotificationTypeV1.Error,
                        "Please select a valid dataset or create a new one"
                    );

                    return;
                }
                if (selectedDatasourceProp === "add-new-datasource") {
                    const newDatasetId = await handleCreateNewDatasource(
                        editedDatasourceProp
                    );

                    if (!newDatasetId) {
                        return;
                    }
                }
                goToDatasetPage();

                return;
            }
            if (
                activeStep ===
                AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS
            ) {
                // TODO: Add
            }
        },
        [activeStep]
    );

    const outletContext = {
        ...(activeStep ===
            AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE && {
            editedDatasource,
            setEditedDatasource,
            selectedDatasource,
            setSelectedDatasource,
        }),
        ...(activeStep ===
            AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS && {
            selectedDatasource,
        }),
    };

    const getNextButtonLabel = (activeStepProp: typeof activeStep): string =>
        ({
            [AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE]:
                t("label.next"),
            [AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS]: t(
                "label.onboard-entity",
                {
                    entity: t("datasets"),
                }
            ),
        }[activeStepProp]);

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                customActions={<Button>{t("label.help")}</Button>}
                subtitle="Connect to StarTree cloud data or add your Pinot datasource"
                title="Let's start setting up your data"
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Box pb={0} pt={2} px={2}>
                            <Typography variant="h5">
                                {/* {t("message.complete-the-following-steps")} */}
                                Complete the following steps
                            </Typography>
                            <StepperV1
                                activeStep={activeStep}
                                stepLabelFn={(step: string): string => {
                                    const stepDefinition = STEPS.find(
                                        (candidate) =>
                                            candidate.subPath === step
                                    );

                                    return t(
                                        `message.${stepDefinition?.translationLabel}`
                                    );
                                }}
                                steps={STEPS.map((item) => item.subPath)}
                            />
                        </Box>
                    </PageContentsCardV1>
                </Grid>
                <Grid item xs={12}>
                    <Outlet context={outletContext} />
                </Grid>
            </PageContentsGridV1>

            <Box marginTop="auto" width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button color="secondary">{t("label.back")}</Button>
                        </Grid>
                        <Grid item>
                            <Button
                                color="primary"
                                onClick={() =>
                                    handleNextClick(
                                        selectedDatasource,
                                        editedDatasource
                                    )
                                }
                            >
                                {getNextButtonLabel(activeStep)}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </PageV1>
    );
};
