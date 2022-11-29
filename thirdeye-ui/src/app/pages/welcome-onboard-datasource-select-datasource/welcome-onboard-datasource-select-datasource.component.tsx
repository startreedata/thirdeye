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

import {
    Box,
    FormControl,
    FormControlLabel,
    FormLabel,
    Grid,
    Radio,
    RadioGroup,
    Typography,
} from "@material-ui/core";
import { AxiosError } from "axios";
import { capitalize } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WizardBottomBar } from "../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    JSONEditorV1,
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDatasources } from "../../rest/datasources/datasources.actions";
import { createDatasource } from "../../rest/datasources/datasources.rest";
import type { Datasource } from "../../rest/dto/datasource.interfaces";
import { createDefaultDatasource } from "../../utils/datasources/datasources.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getDataConfigurationCreateDatasetsPath,
    getDataConfigurationCreatePath,
} from "../../utils/routes/routes.util";
import type {
    DatasourceOptionGroups,
    SelectedDatasource,
} from "./welcome-onboard-datasource-select-datasource.interfaces";
import {
    ADD_NEW_DATASOURCE,
    getDatasourceGroups,
} from "./welcome-onboard-datasource-select-datasource.utils";

export const WelcomeSelectDatasource: FunctionComponent = () => {
    const navigate = useNavigate();

    const [editedDatasource, setEditedDatasource] = useState<Datasource>(
        createDefaultDatasource()
    );
    const [selectedDatasourceName, setSelectedDatasourceName] =
        useState<SelectedDatasource>(null);

    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    const handleDatasourceChange = (value: string): void => {
        setEditedDatasource(JSON.parse(value));
    };

    const handleRadioChange = (
        _e: React.ChangeEvent<HTMLInputElement>,
        value: string
    ): void => {
        setSelectedDatasourceName(value as SelectedDatasource);
    };

    const { datasources, getDatasources, status, errorMessages } =
        useGetDatasources();

    const datasourceGroups = useMemo<DatasourceOptionGroups[]>(
        () => getDatasourceGroups(datasources || [], t),
        [datasources]
    );

    useEffect(() => {
        getDatasources();
    }, []);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasources"),
            })
        );
    }, [status]);

    const goToDatasetPage = useCallback(
        (datasourceName: string) =>
            navigate(getDataConfigurationCreateDatasetsPath(datasourceName)),
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
                    notifyIfErrors(
                        ActionStatus.Error,
                        getErrorMessages(error),
                        notify,
                        t("message.create-error", {
                            entity: t("label.datasource"),
                        })
                    );
                }),
        []
    );

    const handleBack = useCallback(() => {
        navigate(getDataConfigurationCreatePath());
    }, []);

    const handleNext = useCallback((): void | Promise<void> => {
        if (selectedDatasourceName === null) {
            notify(
                NotificationTypeV1.Error,
                "Please select a valid dataset or create a new one"
            );

            return;
        }

        if (selectedDatasourceName === ADD_NEW_DATASOURCE) {
            return handleCreateNewDatasource(editedDatasource).then(
                (created) => {
                    if (!created) {
                        return;
                    }

                    goToDatasetPage(created);
                }
            );
        }

        goToDatasetPage(selectedDatasourceName);
    }, [editedDatasource, selectedDatasourceName]);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Box px={2} py={2}>
                            <Typography variant="h5">
                                {t("message.select-entity", {
                                    entity: t("label.datasource"),
                                })}
                            </Typography>
                            <Typography variant="body2">
                                {capitalize(
                                    t(
                                        "message.you-can-always-add-remove-or-change-entity-in-the-configuration-section",
                                        { entity: t("label.datasource") }
                                    )
                                )}
                            </Typography>
                        </Box>

                        <LoadingErrorStateSwitch
                            isError={status === ActionStatus.Error}
                            isLoading={status === ActionStatus.Working}
                        >
                            {datasourceGroups.map((datasourceGroup) => (
                                <Box key={datasourceGroup.key} px={2} py={1}>
                                    <FormControl component="fieldset">
                                        <FormLabel
                                            color="secondary"
                                            component="legend"
                                        >
                                            {datasourceGroup.title}
                                        </FormLabel>
                                        <RadioGroup
                                            aria-label={t(
                                                "message.select-entity",
                                                {
                                                    entity: t(
                                                        "label.datasource"
                                                    ),
                                                }
                                            )}
                                            name="select-datasource"
                                            value={selectedDatasourceName}
                                            onChange={handleRadioChange}
                                        >
                                            {datasourceGroup.options.map(
                                                (datasourceOption) => (
                                                    <FormControlLabel
                                                        control={<Radio />}
                                                        key={
                                                            datasourceOption.value
                                                        }
                                                        label={
                                                            datasourceOption.label
                                                        }
                                                        value={datasourceOption.value.toString()}
                                                    />
                                                )
                                            )}
                                        </RadioGroup>
                                    </FormControl>
                                </Box>
                            ))}
                        </LoadingErrorStateSwitch>

                        {selectedDatasourceName === ADD_NEW_DATASOURCE ? (
                            <JSONEditorV1<Datasource>
                                hideValidationSuccessIcon
                                value={editedDatasource}
                                onChange={handleDatasourceChange}
                            />
                        ) : null}
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
            <WizardBottomBar
                handleBackClick={handleBack}
                handleNextClick={handleNext}
                nextButtonLabel={t("label.next")}
            />
        </>
    );
};
