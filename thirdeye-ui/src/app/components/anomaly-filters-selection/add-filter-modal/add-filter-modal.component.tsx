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
import { Box, Button, Tab, Tabs } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { DataGridV1 } from "../../../platform/components";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";
import { useUserPreferences } from "../../../utils/user-preferences/user-preferences";
import { UserPreferencesKeys } from "../../../utils/user-preferences/user-preferences.interfaces";
import { Modal } from "../../modal/modal.component";
import { AlertFilter } from "../alert-filter/alert-filter.component";
import { AnomalyFilterQueryStringKey } from "../anomaly-filters-selection.interface";
import { SubscriptionGroupFilter } from "../subscription-group-filter/subscription-group-filter.component";
import { AddFilterModalProps } from "./add-filter-modal.interface";

enum TAB_POSITIONS {
    ALERT,
    SUBSCRIPTION_GROUP,
}

export const AddFilterModal: FunctionComponent<AddFilterModalProps> = ({
    alertsData,
    subscriptionGroupData,
    enumerationItemsData,
}) => {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const { setPreference } = useUserPreferences();

    const [tabPosition, setTabPosition] = useState(TAB_POSITIONS.ALERT);

    const [intermediateSelectedAlerts, setIntermediateSelectedAlerts] =
        useState<Alert[]>([]);

    const [
        intermediateSelectedSubscriptionGroups,
        setIntermediateSelectedSubscriptionGroups,
    ] = useState<SubscriptionGroup[]>([]);

    const syncSelected = (): void => {
        if (alertsData) {
            const currentSelectedAlertIds = searchParams
                .getAll(AnomalyFilterQueryStringKey.ALERT)
                .map((idStr) => Number(idStr));
            setIntermediateSelectedAlerts(
                alertsData.filter((alert) =>
                    currentSelectedAlertIds.includes(alert.id)
                )
            );
        }

        if (subscriptionGroupData) {
            const currentSelectedSubGroupIds = searchParams
                .getAll(AnomalyFilterQueryStringKey.SUBSCRIPTION_GROUP)
                .map((idStr) => Number(idStr));
            setIntermediateSelectedSubscriptionGroups(
                subscriptionGroupData.filter((subscriptionGroup) =>
                    currentSelectedSubGroupIds.includes(subscriptionGroup.id)
                )
            );
        }
    };

    useEffect(() => {
        syncSelected();
    }, [alertsData, subscriptionGroupData]);

    const handleSubmit = (): void => {
        searchParams.delete(AnomalyFilterQueryStringKey.ALERT);
        intermediateSelectedAlerts.forEach((a) =>
            searchParams.append(
                AnomalyFilterQueryStringKey.ALERT,
                a.id.toString()
            )
        );
        setPreference(
            UserPreferencesKeys.ANOMALIES_LIST_DEFAULT_ALERT_FILTERS,
            intermediateSelectedAlerts.map((selected) => selected.id)
        );

        searchParams.delete(AnomalyFilterQueryStringKey.SUBSCRIPTION_GROUP);
        intermediateSelectedSubscriptionGroups.forEach((s) =>
            searchParams.append(
                AnomalyFilterQueryStringKey.SUBSCRIPTION_GROUP,
                s.id.toString()
            )
        );
        setPreference(
            UserPreferencesKeys.ANOMALIES_LIST_DEFAULT_SUBSCRIPTION_FILTERS,
            intermediateSelectedSubscriptionGroups.map(
                (selected) => selected.id
            )
        );

        setSearchParams(searchParams);
    };

    return (
        <Modal
            customTitle={
                <Box pb={2} pt={2}>
                    <Tabs
                        indicatorColor="primary"
                        textColor="primary"
                        value={tabPosition}
                        variant="fullWidth"
                        onChange={(_, v) => setTabPosition(v)}
                    >
                        <Tab
                            label={t("label.alerts")}
                            value={TAB_POSITIONS.ALERT}
                        />
                        <Tab
                            label={t("label.subscription-groups")}
                            value={TAB_POSITIONS.SUBSCRIPTION_GROUP}
                        />
                    </Tabs>
                </Box>
            }
            maxWidth="lg"
            submitButtonLabel={t("label.confirm")}
            trigger={(opeCallback) => {
                return (
                    <Button
                        color="primary"
                        data-testId="modify-filters"
                        size="small"
                        onClick={opeCallback}
                    >
                        {t("label.modify-filters")}
                    </Button>
                );
            }}
            onOpen={() => {
                syncSelected();
            }}
            onSubmit={handleSubmit}
        >
            <Box height={500} minWidth={850}>
                {tabPosition === TAB_POSITIONS.ALERT && (
                    <>
                        {alertsData ? (
                            <AlertFilter
                                alertsData={alertsData}
                                selected={intermediateSelectedAlerts}
                                onSelectionChange={
                                    setIntermediateSelectedAlerts
                                }
                            />
                        ) : (
                            <DataGridV1 columns={[]} data={null} rowKey="id" />
                        )}
                    </>
                )}
                {tabPosition === TAB_POSITIONS.SUBSCRIPTION_GROUP && (
                    <>
                        {alertsData &&
                        subscriptionGroupData &&
                        enumerationItemsData ? (
                            <SubscriptionGroupFilter
                                alertsData={alertsData}
                                enumerationItemsData={enumerationItemsData}
                                selected={
                                    intermediateSelectedSubscriptionGroups
                                }
                                subscriptionGroupData={subscriptionGroupData}
                                onSelectionChange={
                                    setIntermediateSelectedSubscriptionGroups
                                }
                            />
                        ) : (
                            <DataGridV1 columns={[]} data={null} rowKey="id" />
                        )}
                    </>
                )}
            </Box>
        </Modal>
    );
};
