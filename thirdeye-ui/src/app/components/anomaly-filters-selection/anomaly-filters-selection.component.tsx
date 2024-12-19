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
import { Box, Button, Chip, Grid, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { useUserPreferences } from "../../utils/user-preferences/user-preferences";
import { UserPreferencesKeys } from "../../utils/user-preferences/user-preferences.interfaces";
import { EmptyStateSwitch } from "../page-states/empty-state-switch/empty-state-switch.component";
import { AddFilterModal } from "./add-filter-modal/add-filter-modal.component";
import {
    AnomalyFilterQueryStringKey,
    AnomalyFiltersSelectionProps,
    ANOMALY_FILTERS_TEST_IDS,
} from "./anomaly-filters-selection.interface";

export const AnomalyFiltersSelection: FunctionComponent<AnomalyFiltersSelectionProps> =
    ({ subscriptionGroupData, alertsData, enumerationItemsData }) => {
        const { t } = useTranslation();
        const [searchParams, setSearchParams] = useSearchParams();
        const { setPreference } = useUserPreferences();

        const currentSelectedAlerts = useMemo(() => {
            const selectedAlerts = searchParams
                .getAll(AnomalyFilterQueryStringKey.ALERT)
                .map((idStr) => Number(idStr));

            if (selectedAlerts.length > 0 && alertsData) {
                return selectedAlerts
                    .map((alertId) =>
                        alertsData.find((alert) => alert.id === alertId)
                    )
                    .filter((alert) => alert !== undefined);
            }

            return [];
        }, [searchParams, alertsData]) as Alert[];

        const currentSelectedSubGroups = useMemo(() => {
            const selectedSubGroupIds = searchParams
                .getAll(AnomalyFilterQueryStringKey.SUBSCRIPTION_GROUP)
                .map((idStr) => Number(idStr));

            if (selectedSubGroupIds.length > 0 && subscriptionGroupData) {
                return selectedSubGroupIds
                    .map((id) =>
                        subscriptionGroupData.find((alert) => alert.id === id)
                    )
                    .filter((subGroup) => subGroup !== undefined);
            }

            return [];
        }, [searchParams, subscriptionGroupData]) as SubscriptionGroup[];

        const handleDeleteFilter = (
            list: Alert[] | SubscriptionGroup[],
            item: Alert | SubscriptionGroup,
            searchParamKey: AnomalyFilterQueryStringKey,
            userPreferenceKey: UserPreferencesKeys
        ): void => {
            const cloned = [...list].filter((c) => c !== item);
            searchParams.delete(searchParamKey);

            cloned.forEach((entity) =>
                searchParams.append(searchParamKey, entity.id.toString())
            );

            setPreference(
                userPreferenceKey,
                cloned.map((selected) => selected.id)
            );

            setSearchParams(searchParams);
        };

        const handleClearClick = (): void => {
            searchParams.delete(AnomalyFilterQueryStringKey.ALERT);
            searchParams.delete(AnomalyFilterQueryStringKey.SUBSCRIPTION_GROUP);
            setSearchParams(searchParams);

            setPreference(
                UserPreferencesKeys.ANOMALIES_LIST_DEFAULT_SUBSCRIPTION_FILTERS,
                []
            );
            setPreference(
                UserPreferencesKeys.ANOMALIES_LIST_DEFAULT_ALERT_FILTERS,
                []
            );
        };

        return (
            <Box
                border="1px solid rgba(0, 0, 0, 0.15)"
                overflow="wrap"
                padding={1}
            >
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item md={9} sm={8} xl={10} xs={12}>
                        <Grid container alignItems="center" spacing={1}>
                            <EmptyStateSwitch
                                emptyState={
                                    <Grid item>
                                        <Typography
                                            color="textSecondary"
                                            data-testId="filter-text"
                                            variant="caption"
                                        >
                                            {t(
                                                "message.filter-anomalies-by-alert-andor-subscription-group"
                                            )}
                                        </Typography>
                                    </Grid>
                                }
                                isEmpty={
                                    isEmpty(currentSelectedAlerts) &&
                                    isEmpty(currentSelectedSubGroups)
                                }
                            >
                                {currentSelectedAlerts.map((alert) => {
                                    const label = `alert=${alert.name}`;

                                    return (
                                        <Grid item key={label}>
                                            <Chip
                                                clickable
                                                color="primary"
                                                label={label}
                                                size="small"
                                                onClick={() =>
                                                    handleDeleteFilter(
                                                        currentSelectedAlerts,
                                                        alert,
                                                        AnomalyFilterQueryStringKey.ALERT,
                                                        UserPreferencesKeys.ANOMALIES_LIST_DEFAULT_ALERT_FILTERS
                                                    )
                                                }
                                            />
                                        </Grid>
                                    );
                                })}
                                {currentSelectedSubGroups.map((subGroup) => {
                                    const label = `Subscription Group=${subGroup.name}`;

                                    return (
                                        <Grid item key={label}>
                                            <Chip
                                                clickable
                                                color="primary"
                                                label={label}
                                                size="small"
                                                onClick={() =>
                                                    handleDeleteFilter(
                                                        currentSelectedSubGroups,
                                                        subGroup,
                                                        AnomalyFilterQueryStringKey.SUBSCRIPTION_GROUP,
                                                        UserPreferencesKeys.ANOMALIES_LIST_DEFAULT_SUBSCRIPTION_FILTERS
                                                    )
                                                }
                                            />
                                        </Grid>
                                    );
                                })}
                            </EmptyStateSwitch>
                        </Grid>
                    </Grid>

                    <Grid item md={3} sm={4} xl={2} xs={12}>
                        <Grid container justifyContent="flex-end" spacing={1}>
                            <Grid item>
                                <AddFilterModal
                                    alertsData={alertsData}
                                    enumerationItemsData={enumerationItemsData}
                                    subscriptionGroupData={
                                        subscriptionGroupData
                                    }
                                />
                            </Grid>
                            <Grid item>
                                <Button
                                    color="primary"
                                    data-testId="clear-filters"
                                    data-testid={
                                        ANOMALY_FILTERS_TEST_IDS.CLEAR_BTN
                                    }
                                    disabled={
                                        isEmpty(currentSelectedAlerts) &&
                                        isEmpty(currentSelectedSubGroups)
                                    }
                                    size="small"
                                    onClick={handleClearClick}
                                >
                                    {t("label.clear")}
                                </Button>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Box>
        );
    };
