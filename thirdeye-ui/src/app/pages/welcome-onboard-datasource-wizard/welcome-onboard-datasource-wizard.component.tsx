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
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useLocation, useNavigate, useParams } from "react-router-dom";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    StepperV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { onBoardDataset } from "../../rest/datasets/datasets.rest";
import { createDatasource } from "../../rest/datasources/datasources.rest";
import type { Datasource } from "../../rest/dto/datasource.interfaces";
import { createDefaultDatasource } from "../../utils/datasources/datasources.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    AppRouteRelative,
    getDataConfigurationCreateDatasetsPath,
    getWelcomeLandingPath,
} from "../../utils/routes/routes.util";
import type { SelectedDatasource } from "../welcome-onboard-datasource-select-datasource/welcome-onboard-datasource-select-datasource.interfaces";
import { ADD_NEW_DATASOURCE } from "../welcome-onboard-datasource-select-datasource/welcome-onboard-datasource-select-datasource.utils";

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
    const [selectedDatasourceName, setSelectedDatasourceName] =
        useState<SelectedDatasource>(null);

    const [selectedDatasets, setSelectedDatasets] = useState<
        Record<string, boolean>
    >({});

    const queryParams: { id?: string } = useParams();
    const queryDatasourceName = queryParams.id;

    const activeStep = useMemo(() => {
        // Tries to extract the last part of the url for
        // Uses the whole url if nothing comes up
        // This is required since the first url substring ("dataset")
        // is a part of the greater url for this module
        const urlPath: string =
            pathname.split("/").filter(Boolean).pop() || pathname;

        const activeStepDefinition = STEPS.find((candidate) =>
            candidate.subPath.includes(urlPath)
        );

        // Fallback
        if (!activeStepDefinition) {
            return STEPS[0].subPath;
        }

        return activeStepDefinition.subPath;
    }, [pathname]);

    useEffect(() => {
        if (
            activeStep ===
            AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE
        ) {
            setSelectedDatasourceName(null);
        }
        if (
            activeStep === AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS
        ) {
            if (queryDatasourceName) {
                setSelectedDatasourceName(queryDatasourceName);
            }
        }
    }, [activeStep, queryDatasourceName]);

    const goToDatasetPage = useCallback(
        (datasourceName: string) =>
            navigate(getDataConfigurationCreateDatasetsPath(datasourceName)),
        []
    );

    const goToLandingPage = useCallback(
        () => navigate(getWelcomeLandingPath()),
        []
    );

    const handleCreateNewDatasource = useCallback(
        (editedDatasourceProp: Datasource) =>
            createDatasource(editedDatasourceProp)
                .then((datasource: Datasource): string => {
                    notify(
                        NotificationTypeV1.Success,
                        t("message.create-success", {
                            entity: t("label.datasource"),
                        })
                    );

                    setSelectedDatasourceName(datasource.name);

                    return datasource.name;
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

    const handleOnboardDatasets = useCallback(
        (datasetsName: string[], datasourceName: string) =>
            Promise.all(
                datasetsName.map((datasetName) =>
                    onBoardDataset(datasetName, datasourceName)
                        .then(() => {
                            notify(
                                NotificationTypeV1.Success,
                                t("message.onboard-success", {
                                    entity: t("label.dataset"),
                                })
                            );
                            // Redirect to welcome landing
                            navigate(getWelcomeLandingPath());

                            return Promise.resolve();
                        })
                        .catch((error: AxiosError) => {
                            const errMessages = getErrorMessages(error);

                            isEmpty(errMessages)
                                ? notify(
                                      NotificationTypeV1.Error,
                                      t("message.onboard-error", {
                                          entity: t("label.dataset"),
                                      })
                                  )
                                : errMessages.map((err) =>
                                      notify(NotificationTypeV1.Error, err)
                                  );

                            return Promise.reject();
                        })
                )
            ),
        []
    );

    const handleNextClick = useCallback(
        (activeStepProp: typeof activeStep) => {
            if (
                activeStepProp ===
                AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE
            ) {
                return ({
                    selectedDatasourceNameProp = null,
                    editedDatasourceProp,
                }: {
                    selectedDatasourceNameProp: SelectedDatasource;
                    editedDatasourceProp: Datasource;
                }) => {
                    if (selectedDatasourceNameProp === null) {
                        notify(
                            NotificationTypeV1.Error,
                            "Please select a valid dataset or create a new one"
                        );

                        return;
                    }
                    if (selectedDatasourceNameProp === ADD_NEW_DATASOURCE) {
                        return handleCreateNewDatasource(
                            editedDatasourceProp
                        ).then((created) => {
                            if (!created) {
                                return;
                            }

                            goToDatasetPage(created);
                        });
                    }
                    goToDatasetPage(selectedDatasourceNameProp);

                    return;
                };
            }

            if (
                activeStepProp ===
                AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS
            ) {
                return ({
                    datasetsNameProp = [],
                    selectedDatasourceNameProp,
                }: {
                    datasetsNameProp: string[];
                    selectedDatasourceNameProp: SelectedDatasource;
                }) => {
                    if (selectedDatasourceNameProp) {
                        handleOnboardDatasets(
                            datasetsNameProp,
                            selectedDatasourceNameProp
                        ).then(() => {
                            goToLandingPage();
                        });
                    }

                    return;
                };
            }

            // To handle unexpected cases and
            // maintain function return type consistency
            return () => Promise.reject();
        },

        []
    );

    const outletContext = {
        ...(activeStep ===
            AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE && {
            editedDatasource,
            setEditedDatasource,
            selectedDatasourceName,
            setSelectedDatasourceName,
        }),
        ...(activeStep ===
            AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS && {
            selectedDatasourceName,
            selectedDatasets,
            setSelectedDatasets,
        }),
    };

    const getNextButtonLabel = useCallback(
        (activeStepProp: typeof activeStep): string =>
            ({
                [AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE]:
                    t("label.next"),
                [AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS]: t(
                    "label.onboard-entity",
                    {
                        entity: t("datasets"),
                    }
                ),
            }[activeStepProp]),
        []
    );

    const handleNextProps = {
        selectedDatasourceNameProp: selectedDatasourceName,
        editedDatasourceProp: editedDatasource,
        datasetsNameProp: Object.entries(selectedDatasets)
            .filter(([, v]: [string, boolean]) => v)
            .map(([k]: [string, boolean]) => k),
    };

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
                                    handleNextClick(activeStep)(handleNextProps)
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
