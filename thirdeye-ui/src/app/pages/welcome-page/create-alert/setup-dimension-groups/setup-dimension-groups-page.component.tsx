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
import {
    default as React,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AvailableAlgorithmOption } from "../../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.interfaces";
import { generateTemplateProperties } from "../../../../components/alert-wizard-v3/threshold-setup/threshold-setup.utils";
import { CohortsTable } from "../../../../components/cohort-detector/cohorts-table/cohorts-table.component";
import { DatasetDetails } from "../../../../components/cohort-detector/dataset-details/dataset-details.component";
import { WizardBottomBar } from "../../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { CohortResult } from "../../../../rest/dto/rca.interfaces";
import { useGetCohort } from "../../../../rest/rca/rca.actions";
import { GetCohortParams } from "../../../../rest/rca/rca.interfaces";
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import { AppRouteRelative } from "../../../../utils/routes/routes.util";

export const SetupDimensionGroupsPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const { cohortsResponse, getCohorts, status, errorMessages } =
        useGetCohort();

    const { alert, handleAlertPropertyChange, selectedAlgorithmOption } =
        useOutletContext<{
            alert: EditableAlert;
            handleAlertPropertyChange: (
                contents: Partial<EditableAlert>,
                isTotalChange?: boolean
            ) => void;
            selectedAlgorithmOption: AvailableAlgorithmOption;
        }>();

    const [selectedCohorts, setSelectedCohorts] = useState<CohortResult[]>([]);

    useEffect(() => {
        // On initial render, ensure the alert template is a dimension exploration one
        if (
            selectedAlgorithmOption.algorithmOption
                .alertTemplateForMultidimension !== alert.template?.name
        ) {
            return navigate(
                `../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`
            );
        }
    }, []);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.cohorts-data"),
            })
        );
    }, [status]);

    const handleSearchButtonClick = (
        getCohortsParams: GetCohortParams
    ): void => {
        getCohorts(getCohortsParams);
    };

    const handleCohortsSelectionChange = (cohorts: CohortResult[]): void => {
        setSelectedCohorts(cohorts);
    };

    const handleCreateBtnClick = (): void => {
        const enumerationItemConfiguration = selectedCohorts.map((cohort) => {
            const criteria = Object.keys(cohort.dimensionFilters).map((k) => {
                return `${k}='${cohort.dimensionFilters[k]}'`;
            });
            const joined = criteria.join(" AND ");

            return {
                params: {
                    queryFilters: ` AND ${joined}`,
                },
            };
        });

        handleAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                queryFilters: "${queryFilters}",
                enumerationItems: enumerationItemConfiguration,
            },
        });

        navigate(
            `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`
        );
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <Typography variant="h5">
                        {t("message.multidimension-setup", {
                            algorithmName: `(${selectedAlgorithmOption.algorithmOption.title})`,
                        })}
                    </Typography>
                    <Typography variant="body1">
                        {t(
                            "message.automatically-detects-and-prioritizes-dimensions"
                        )}
                    </Typography>
                </Grid>
                <Grid item xs={12}>
                    <DatasetDetails
                        submitButtonLabel={t(
                            "message.generate-dimensions-to-monitor"
                        )}
                        subtitle={t(
                            "message.automatically-detects-dimensions-based-on-your-selection"
                        )}
                        title={t("label.automated-generator")}
                        onAggregationFunctionSelect={(aggFunc) => {
                            handleAlertPropertyChange({
                                templateProperties: {
                                    ...alert.templateProperties,
                                    aggregationFunction: aggFunc,
                                },
                            });
                        }}
                        onMetricSelect={(
                            metric,
                            dataset,
                            aggregationFunction
                        ) => {
                            handleAlertPropertyChange({
                                templateProperties: {
                                    ...alert.templateProperties,
                                    ...generateTemplateProperties(
                                        metric,
                                        dataset,
                                        aggregationFunction
                                    ),
                                },
                            });
                        }}
                        onSearchButtonClick={handleSearchButtonClick}
                    />
                </Grid>
                <Grid item xs={12}>
                    <CohortsTable
                        cohortsData={cohortsResponse}
                        getCohortsRequestStatus={status}
                        subtitle={t(
                            "message.select-the-dimensions-and-create-a-multi-dimension-alert"
                        )}
                        title={t("label.dimensions-and-outliers-results")}
                        onSelectionChange={handleCohortsSelectionChange}
                    >
                        <Box textAlign="right">
                            <Button
                                color="primary"
                                disabled={selectedCohorts.length === 0}
                                onClick={handleCreateBtnClick}
                            >
                                {t("label.create-multidimension-alert")}
                            </Button>
                        </Box>
                    </CohortsTable>
                </Grid>
            </PageContentsGridV1>

            <WizardBottomBar
                backBtnLink={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`}
                nextBtnLink={
                    alert.templateProperties?.enumerationItems
                        ? `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`
                        : undefined
                }
            />
        </>
    );
};
