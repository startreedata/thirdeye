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

import { Box, Divider, FormGroup, Grid, Typography } from "@material-ui/core";
import type { AxiosError } from "axios";
import { capitalize, isEmpty } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { SelectDatasetOption } from "../../components/welcome-onboard-datasource/select-dataset-option/select-dataset-option.component";
import { WizardBottomBar } from "../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDatasets } from "../../rest/datasets/datasets.actions";
import { onBoardDataset } from "../../rest/datasets/datasets.rest";
import { useGetDatasourceByName } from "../../rest/datasources/datasources.actions";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getWelcomeLandingPath } from "../../utils/routes/routes.util";
import { SELECT_ALL } from "./welcome-onboard-datasource-select-datasets.utils";

export const WelcomeSelectDatasets: FunctionComponent = () => {
    const [selectedDatasets, setSelectedDatasets] = useState<
        Record<string, boolean>
    >({});

    const queryParams: { id?: string } = useParams();
    const selectedDatasourceName = queryParams.id;

    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const navigate = useNavigate();

    const handleToggleCheckbox = useCallback(
        (event: React.ChangeEvent<HTMLInputElement>): void => {
            const { name, checked } = event.target;
            if (!name) {
                return;
            }
            if (name === SELECT_ALL) {
                setSelectedDatasets((selectedStateProp) => {
                    const areAllCheckedState = !Object.values(
                        selectedStateProp
                    ).some((v) => !v);
                    const newState = Object.assign(
                        {},
                        ...Object.keys(selectedStateProp).map((k) => ({
                            [k]: !areAllCheckedState,
                        }))
                    );

                    return newState;
                });

                return;
            }

            setSelectedDatasets((selectedStateProp) => ({
                ...selectedStateProp,
                [name]: checked,
            }));
        },
        []
    );

    const {
        datasource,
        getDatasourceByName,
        status: datasourceStatus,
    } = useGetDatasourceByName();

    useEffect(() => {
        if (selectedDatasourceName) {
            getDatasourceByName(selectedDatasourceName);
        }
    }, []);

    const {
        datasets,
        getDatasets,
        status: datasetsStatus,
        errorMessages,
    } = useGetDatasets();

    useEffect(() => {
        getDatasets().then((datasetsProp) => {
            if (!datasetsProp) {
                return;
            }
            setSelectedDatasets(
                Object.assign(
                    {},
                    ...datasetsProp?.map(({ name }) => ({ [name]: false }))
                )
            );
        });
    }, []);

    useEffect(() => {
        notifyIfErrors(
            datasetsStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasets"),
            })
        );
    }, [datasetsStatus]);

    useEffect(() => {
        notifyIfErrors(
            datasourceStatus,
            errorMessages,
            notify,
            capitalize(
                t("message.error-while-fetching", {
                    entity: t("label.datasources"),
                })
            )
        );
    }, [datasourceStatus]);

    const areSomeChecked = Object.values(selectedDatasets).some((v) => v);
    const areAllChecked = !Object.values(selectedDatasets).some((v) => !v);

    const selectAllProps = useMemo(
        () => ({
            checked: areAllChecked,
            indeterminate: areSomeChecked && !areAllChecked,
            labelPrimaryText: "Select All",
            name: SELECT_ALL,
            onChange: handleToggleCheckbox,
        }),
        [areSomeChecked, areAllChecked]
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

    const handleNext = useCallback(() => {
        if (!selectedDatasourceName) {
            return;
        }

        const datasetsName = Object.entries(selectedDatasets)
            .filter(([, v]: [string, boolean]) => v)
            .map(([k]: [string, boolean]) => k);

        handleOnboardDatasets(datasetsName, selectedDatasourceName).then(() => {
            navigate(getWelcomeLandingPath());
        });
    }, [selectedDatasets, selectedDatasourceName]);

    const handleBack = useCallback(() => {
        navigate(-1);
    }, []);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <LoadingErrorStateSwitch
                        isError={datasourceStatus === ActionStatus.Error}
                        isLoading={datasourceStatus === ActionStatus.Working}
                    >
                        <PageContentsCardV1>
                            <Box px={2} py={2}>
                                <Typography variant="h5">
                                    Onboard datasets for {datasource?.name}
                                </Typography>
                                <Typography variant="body2">
                                    Select the datasets you want to include in
                                    your configuration
                                </Typography>
                                <LoadingErrorStateSwitch
                                    isError={
                                        datasetsStatus === ActionStatus.Error
                                    }
                                    isLoading={
                                        datasetsStatus === ActionStatus.Working
                                    }
                                >
                                    <Box
                                        alignItems="flexStart"
                                        display="flex"
                                        mt={2}
                                    >
                                        <FormGroup>
                                            <SelectDatasetOption
                                                {...selectAllProps}
                                            />
                                            <Divider />

                                            {datasets?.map((dataset) => (
                                                <SelectDatasetOption
                                                    checked={
                                                        !!selectedDatasets?.[
                                                            dataset.name
                                                        ]
                                                    }
                                                    key={dataset.name}
                                                    labelPrimaryText={
                                                        dataset.name
                                                    }
                                                    labelSecondaryText={`${dataset.dimensions.length} dimensions`}
                                                    name={dataset.name}
                                                    onChange={
                                                        handleToggleCheckbox
                                                    }
                                                />
                                            ))}
                                        </FormGroup>
                                    </Box>
                                </LoadingErrorStateSwitch>
                            </Box>
                        </PageContentsCardV1>
                    </LoadingErrorStateSwitch>
                </Grid>
            </PageContentsGridV1>
            <WizardBottomBar
                handleBackClick={handleBack}
                handleNextClick={handleNext}
                nextButtonLabel={t("label.onboard-entity", {
                    entity: t("label.datasets"),
                })}
            />
        </>
    );
};
