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

import {
    Box,
    Button,
    Divider,
    FormGroup,
    Grid,
    Typography,
} from "@material-ui/core";
import type { AxiosError } from "axios";
import { capitalize } from "lodash";
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
import { SelectDatasetOption } from "../../../../components/welcome-onboard-datasource/select-dataset-option/select-dataset-option.component";
import { WizardBottomBar } from "../../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { onBoardDataset } from "../../../../rest/datasets/datasets.rest";
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

export const WelcomeSelectDatasets: FunctionComponent = () => {
    const [selectedDatasets, setSelectedDatasets] = useState<string[]>([]);
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

    const handleToggleCheckbox = useCallback(
        (event: React.ChangeEvent<HTMLInputElement>): void => {
            const { name, checked } = event.target;

            if (!name) {
                return;
            }

            setSelectedDatasets((currentlySelected) => {
                if (!checked) {
                    return currentlySelected.filter((item) => item !== name);
                }

                return [...currentlySelected, name];
            });
        },
        []
    );

    useEffect(() => {
        if (datasourceId) {
            getDatasource(Number(datasourceId));
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

    const handleOnboardDatasets = useCallback(
        (datasetNames: string[], datasourceId: string) =>
            Promise.all(
                datasetNames.map((datasetName) =>
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
                            notifyIfErrors(
                                ActionStatus.Error,
                                getErrorMessages(error),
                                notify,
                                t("message.onboard-error", {
                                    entity: t("label.dataset"),
                                })
                            );

                            return Promise.reject();
                        })
                )
            ),
        []
    );

    const handleNext = useCallback(() => {
        if (!datasourceId) {
            return;
        }

        handleOnboardDatasets(selectedDatasets, datasourceId).then(() => {
            navigate(getWelcomeLandingPath());
        });
    }, [selectedDatasets, datasourceId]);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
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
                            <Box px={2} py={2}>
                                <Typography variant="h5">
                                    {t(
                                        "message.onboard-datasource-onboard-datasets-for",
                                        {
                                            datasetName: datasource?.name,
                                        }
                                    )}
                                </Typography>
                                <Typography variant="body2">
                                    {t(
                                        "message.select-the-datasets-you-want-to-include-in-your-configuration"
                                    )}
                                </Typography>
                                <LoadingErrorStateSwitch
                                    wrapInCard
                                    wrapInGrid
                                    isError={
                                        getTablesStatus === ActionStatus.Error
                                    }
                                    isLoading={
                                        getTablesStatus === ActionStatus.Working
                                    }
                                >
                                    <EmptyStateSwitch
                                        emptyState={
                                            <Box p={5}>
                                                {t(
                                                    "message.no-datasets-available"
                                                )}
                                            </Box>
                                        }
                                        isEmpty={tables?.length === 0}
                                    >
                                        <Box
                                            alignItems="flexStart"
                                            data-testid={
                                                ONBOARD_DATASETS_TEST_IDS.DATASETS_OPTIONS_CONTAINER
                                            }
                                            display="flex"
                                            id="datasets-options-container"
                                            mt={2}
                                        >
                                            <FormGroup>
                                                <Button
                                                    color="primary"
                                                    variant="text"
                                                    onClick={() => {
                                                        tables &&
                                                            setSelectedDatasets(
                                                                tables.map(
                                                                    ({
                                                                        name,
                                                                    }) => name
                                                                )
                                                            );
                                                    }}
                                                >
                                                    {t("label.select-all")}
                                                </Button>
                                                <Divider />

                                                {tables?.map((dataset) => (
                                                    <SelectDatasetOption
                                                        checked={selectedDatasets.includes(
                                                            dataset.name
                                                        )}
                                                        key={dataset.name}
                                                        labelPrimaryText={
                                                            dataset.name
                                                        }
                                                        labelSecondaryText={t(
                                                            "label.num-dimensions",
                                                            {
                                                                num: dataset
                                                                    .dimensions
                                                                    .length,
                                                            }
                                                        )}
                                                        name={dataset.name}
                                                        onChange={
                                                            handleToggleCheckbox
                                                        }
                                                    />
                                                ))}
                                            </FormGroup>
                                        </Box>
                                    </EmptyStateSwitch>
                                </LoadingErrorStateSwitch>
                            </Box>
                        </PageContentsCardV1>
                    </LoadingErrorStateSwitch>
                </Grid>
            </PageContentsGridV1>
            <WizardBottomBar
                backBtnLink={AppRoute.WELCOME_ONBOARD_DATASOURCE}
                handleNextClick={handleNext}
                nextButtonLabel={t("label.onboard-entity", {
                    entity: t("label.datasets"),
                })}
            />
        </>
    );
};
