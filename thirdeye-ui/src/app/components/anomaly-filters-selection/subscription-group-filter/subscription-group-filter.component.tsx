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
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridColumnV1,
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
} from "../../../platform/components";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";
import { UiSubscriptionGroup } from "../../../rest/dto/ui-subscription-group.interfaces";
import { getUiSubscriptionGroups } from "../../../utils/subscription-groups/subscription-groups.util";
import { ANOMALY_FILTERS_TEST_IDS } from "../anomaly-filters-selection.interface";
import { ExpandedRow } from "./expanded-row/expanded-row.component";
import { SubscriptionGroupFilterProps } from "./subscription-group-filter.interface";

export const SubscriptionGroupFilter: FunctionComponent<SubscriptionGroupFilterProps> =
    ({
        subscriptionGroupData,
        alertsData,
        enumerationItemsData,
        onSelectionChange,
        selected,
    }) => {
        const { t } = useTranslation();

        const [uiSubscriptionGroups, setUiSubscriptionGroups] =
            useState<UiSubscriptionGroup[]>();

        const intermediateSelectedSubGroups = useMemo(() => {
            const selectedIds = selected.map((selected) => selected.id);

            if (uiSubscriptionGroups) {
                return uiSubscriptionGroups.filter((subscriptionGroup) =>
                    selectedIds.includes(subscriptionGroup.id)
                );
            }

            return [];
        }, [selected, uiSubscriptionGroups]);

        // SelectionModel to show selection on data-grid
        const selectionModel: DataGridSelectionModelV1<UiSubscriptionGroup> =
            useMemo(
                () => ({
                    rowKeyValues: intermediateSelectedSubGroups.map(
                        (a: UiSubscriptionGroup) => a.id
                    ),
                    rowKeyValueMap: new Map(
                        intermediateSelectedSubGroups.map(
                            (UiSubscriptionGroup) => [
                                UiSubscriptionGroup.id,
                                UiSubscriptionGroup,
                            ]
                        )
                    ),
                }),
                [intermediateSelectedSubGroups]
            );

        const alertColumns: DataGridColumnV1<UiSubscriptionGroup>[] =
            useMemo(() => {
                return [
                    {
                        key: "name",
                        dataKey: "name",
                        header: t("label.name"),
                        minWidth: 0,
                        flex: 1.5,
                        sortable: true,
                    },
                    {
                        key: "alertCount",
                        dataKey: "alertCount",
                        header: t("label.alerts"),
                        minWidth: 0,
                        flex: 1,
                    },
                    {
                        key: "dimensionCount",
                        dataKey: "dimensionCount",
                        header: t("label.dimensions"),
                        minWidth: 0,
                        flex: 1,
                    },
                ];
            }, []);

        useEffect(() => {
            setUiSubscriptionGroups(
                getUiSubscriptionGroups(
                    subscriptionGroupData,
                    alertsData,
                    enumerationItemsData
                ).map((uiSubscriptionGroup, idx) => {
                    const expandedContent: { children?: unknown } = {};

                    if (uiSubscriptionGroup.alerts.length > 0) {
                        expandedContent.children = [
                            {
                                id: idx,
                                expandPanelContents: (
                                    <ExpandedRow
                                        uiSubscriptionGroup={
                                            uiSubscriptionGroup
                                        }
                                    />
                                ),
                            },
                        ];
                    }

                    return {
                        ...uiSubscriptionGroup,
                        ...expandedContent,
                    };
                })
            );
        }, [subscriptionGroupData, alertsData, enumerationItemsData]);

        const handleSelectionChange = (
            newSelected: DataGridSelectionModelV1<UiSubscriptionGroup>
        ): void => {
            onSelectionChange(
                Array.from(newSelected?.rowKeyValueMap?.values() || []).map(
                    (s: UiSubscriptionGroup) =>
                        s.subscriptionGroup as SubscriptionGroup
                )
            );
        };

        return (
            <DataGridV1<UiSubscriptionGroup>
                columns={alertColumns}
                data={uiSubscriptionGroups as UiSubscriptionGroup[]}
                data-testid={ANOMALY_FILTERS_TEST_IDS.SUBSCRIPTION_GROUP_TABLE}
                expandColumnKey="name"
                initialSortState={{
                    key: "name",
                    order: DataGridSortOrderV1.ASC,
                }}
                rowKey="id"
                scroll={DataGridScrollV1.Contents}
                searchPlaceholder={t("label.search-entity", {
                    entity: t("label.subscription-groups"),
                })}
                selectionModel={selectionModel}
                toolbarComponent={
                    <>
                        <span>
                            {subscriptionGroupData.length}{" "}
                            {t("label.subscription-groups")}
                        </span>
                        <span>|</span>
                        <span>
                            {intermediateSelectedSubGroups?.length}{" "}
                            {t("label.selected")}
                        </span>
                    </>
                }
                onSelectionChange={handleSelectionChange}
            />
        );
    };
