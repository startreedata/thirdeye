/*
 * Copyright 2023 StarTree Inc
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

import { Box, Button, Divider, Typography } from "@material-ui/core";
import type { AxiosError } from "axios";
import { capitalize, isEmpty } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { EmptyStateSwitch } from "../../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WizardBottomBar } from "../../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import {
    createDemoDatasets,
    onBoardDataset,
} from "../../../../rest/datasets/datasets.rest";
import {
    useGetDatasource,
    useGetTablesForDatasourceID,
} from "../../../../rest/datasources/datasources.actions";
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../../../utils/rest/rest.util";
import {
    AppRoute,
    getWelcomeLandingPath,
} from "../../../../utils/routes/routes.util";
import { ONBOARD_DATASETS_TEST_IDS } from "./onboard-datasets-page.interface";
import { Alert } from "@material-ui/lab";
import InfoOutlined from "@material-ui/icons/InfoOutlined";
import { useGetDemoDatasets } from "../../../../rest/datasets/datasets.actions";
import { DatasetList } from "./dataset-list";
import { useOnBoardDatasetStyles } from "./styles";

export const WelcomeSelectDatasets: FunctionComponent = () => {
    const classes = useOnBoardDatasetStyles();
    const [selectedDatasets, setSelectedDatasets] = useState<string[]>([]);
    const [selectedDemoDatasets, setSelectedDemoDatasets] = useState<string[]>(
        []
    );
    const [isLoading, setIsLoading] = useState(false);
    const [onboardingError, setOnboardingError] = useState<AxiosError | null>(
        null
    );
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { id: datasourceId } = useParams<{ id: string }>();

    const {
        tables,
        getTableForDatasourceID,
        status: getTablesStatus,
        errorMessages,
    } = useGetTablesForDatasourceID();

    const {
        getDatasource,
        datasource,
        status: getDataSourceStatus,
        errorMessages: getDatSourceErrorMessages,
    } = useGetDatasource();

    const {
        datasets: demoDatasets,
        getDemoDatasets,
        status: getDemoDatasetsStatus,
        errorMessages: getDemoDatasetsErrorMessages,
    } = useGetDemoDatasets();

    const handleDatasetSelect = useCallback(
        (
            event: React.ChangeEvent<HTMLInputElement>,
            datasetType: "selectedSourceDatasets" | "demoDatasets"
        ): void => {
            const { name, checked } = event.target;

            if (!name) {
                return;
            }

            if (datasetType === "selectedSourceDatasets") {
                setSelectedDatasets((currentlySelected) => {
                    if (!checked) {
                        return currentlySelected.filter(
                            (item) => item !== name
                        );
                    }

                    return [...currentlySelected, name];
                });
            } else {
                setSelectedDemoDatasets((currentlySelected) => {
                    if (!checked) {
                        return currentlySelected.filter(
                            (item) => item !== name
                        );
                    }

                    return [...currentlySelected, name];
                });
            }
        },
        []
    );

    useEffect(() => {
        if (datasourceId) {
            getDatasource(Number(datasourceId));
            getDemoDatasets(Number(datasourceId));
            getTableForDatasourceID(Number(datasourceId)).then((datasets) => {
                if (!datasets) {
                    return;
                }

                setSelectedDatasets(datasets.map(({ name }) => name));
            });
        } else {
            navigate(AppRoute.WELCOME_ONBOARD_DATASOURCE);
        }
    }, []);

    useEffect(() => {
        notifyIfErrors(
            getTablesStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasets"),
            })
        );
    }, [getTablesStatus]);

    useEffect(() => {
        notifyIfErrors(
            getDataSourceStatus,
            getDatSourceErrorMessages,
            notify,
            t("message.error-while-fetching-datasource")
        );
    }, [getTablesStatus]);

    useEffect(() => {
        notifyIfErrors(
            getDemoDatasetsStatus,
            getDemoDatasetsErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.demo-datasets"),
            })
        );
    }, [getDemoDatasetsStatus]);

    useEffect(() => {
        notifyIfErrors(
            getTablesStatus,
            errorMessages,
            notify,
            capitalize(
                t("message.error-while-fetching", {
                    entity: t("label.datasources"),
                })
            )
        );
    }, [getTablesStatus]);

    useEffect(() => {
        if (onboardingError) {
            notifyIfErrors(
                ActionStatus.Error,
                getErrorMessages(onboardingError),
                notify,
                t("message.onboard-error", {
                    entity: t("label.dataset"),
                })
            );
        }
    }, [onboardingError]);

    const handleOnboardDatasets = useCallback(
        (
            datasetNames: string[],
            selectedDemoDatasets: string[],
            datasourceId: string
        ) =>
            Promise.all([
                ...datasetNames.map((datasetName) =>
                    onBoardDataset(datasetName, datasourceId)
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
                            return Promise.reject(error);
                        })
                ),
                ...selectedDemoDatasets.map((datasetName) =>
                    createDemoDatasets(datasetName, Number(datasourceId))
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
                            return Promise.reject(error);
                        })
                ),
            ])
                .then(() => {
                    setOnboardingError(null);
                })
                .catch((error) => {
                    setOnboardingError(error);

                    return Promise.reject();
                })
                .finally(() => {
                    setIsLoading(false);
                }),
        []
    );

    const handleNext = useCallback(() => {
        if (!datasourceId) {
            return;
        }
        setIsLoading(true);
        handleOnboardDatasets(
            selectedDatasets,
            selectedDemoDatasets,
            datasourceId
        ).then(() => {
            setIsLoading(false);
            navigate(getWelcomeLandingPath());
        });
    }, [selectedDatasets, datasourceId]);

    const isNextButtonDisabled =
        tables?.length === 0 || isEmpty(selectedDatasets);

    return (
        <>
            <Box
                alignContent="center"
                className={classes.container}
                display="flex"
                flexDirection="column"
            >
                <LoadingErrorStateSwitch
                    wrapInCard
                    wrapInGrid
                    isError={getTablesStatus === ActionStatus.Error}
                    isLoading={
                        getTablesStatus === ActionStatus.Working ||
                        getTablesStatus === ActionStatus.Initial
                    }
                >
                    <PageContentsCardV1>
                        <Box
                            alignContent="center"
                            display="flex"
                            flexDirection="column"
                        >
                            <Typography align="center" variant="h5">
                                {t("message.select-your-datasets")}
                            </Typography>
                            <Typography align="center" variant="body2">
                                {t("message.add-data-to-te-from-source")}
                            </Typography>
                            <LoadingErrorStateSwitch
                                wrapInCard
                                wrapInGrid
                                isError={getTablesStatus === ActionStatus.Error}
                                isLoading={
                                    getTablesStatus === ActionStatus.Working
                                }
                            >
                                <EmptyStateSwitch
                                    emptyState={
                                        <Box p={5}>
                                            <Alert
                                                icon={<InfoOutlined />}
                                                severity="info"
                                                variant="outlined"
                                            >
                                                {t(
                                                    "message.no-datasets-available-in-datasource"
                                                )}
                                            </Alert>
                                        </Box>
                                    }
                                    isEmpty={
                                        isEmpty(tables) && isEmpty(demoDatasets)
                                    }
                                >
                                    <Box
                                        data-testid={
                                            ONBOARD_DATASETS_TEST_IDS.DATASETS_OPTIONS_CONTAINER
                                        }
                                        display="flex"
                                        gridGap="64px"
                                        id="datasets-options-container"
                                        justifyContent="center"
                                        mt={2}
                                    >
                                        <div
                                            style={{
                                                maxHeight: "300px",
                                                overflow: "auto",
                                            }}
                                        >
                                            <DatasetList
                                                datasetGroup={datasource?.name}
                                                datasets={tables}
                                                selectedDatasets={
                                                    selectedDatasets
                                                }
                                                onSelectDataset={(
                                                    e: React.ChangeEvent<HTMLInputElement>
                                                ) =>
                                                    handleDatasetSelect(
                                                        e,
                                                        "selectedSourceDatasets"
                                                    )
                                                }
                                            />
                                        </div>
                                        <DatasetList
                                            datasetGroup="Sample Datasets"
                                            datasets={demoDatasets}
                                            selectedDatasets={
                                                selectedDemoDatasets
                                            }
                                            onSelectDataset={(
                                                e: React.ChangeEvent<HTMLInputElement>
                                            ) =>
                                                handleDatasetSelect(
                                                    e,
                                                    "demoDatasets"
                                                )
                                            }
                                        />
                                    </Box>
                                    <Box display="flex" justifyContent="center">
                                        <Button
                                            color="primary"
                                            variant="text"
                                            onClick={() => {
                                                tables &&
                                                    setSelectedDatasets(
                                                        tables.map(
                                                            ({ name }) => name
                                                        )
                                                    );
                                                demoDatasets &&
                                                    setSelectedDemoDatasets(
                                                        demoDatasets.map(
                                                            ({ name }) => name
                                                        )
                                                    );
                                            }}
                                        >
                                            {t("label.select-all")}
                                        </Button>
                                        <Divider
                                            orientation="vertical"
                                            variant="middle"
                                        />
                                        <Button
                                            color="primary"
                                            variant="text"
                                            onClick={() => {
                                                setSelectedDatasets([]);
                                                setSelectedDemoDatasets([]);
                                            }}
                                        >
                                            {t("label.deselect-all")}
                                        </Button>
                                    </Box>
                                </EmptyStateSwitch>
                            </LoadingErrorStateSwitch>
                        </Box>
                    </PageContentsCardV1>
                </LoadingErrorStateSwitch>
            </Box>
            <WizardBottomBar
                backBtnLink={AppRoute.WELCOME_ONBOARD_DATASOURCE}
                handleNextClick={handleNext}
                isLoading={isLoading}
                nextButtonIsDisabled={isNextButtonDisabled}
                nextButtonLabel={t("label.onboard-entity", {
                    entity: t("label.datasets"),
                })}
            />
        </>
    );
};
