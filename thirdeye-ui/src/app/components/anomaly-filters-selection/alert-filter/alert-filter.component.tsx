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
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridColumnV1,
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
} from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { ANOMALY_FILTERS_TEST_IDS } from "../anomaly-filters-selection.interface";
import { AlertFilterProps } from "./alert-filter.interface";

export const AlertFilter: FunctionComponent<AlertFilterProps> = ({
    alertsData,
    selected,
    onSelectionChange,
}) => {
    const { t } = useTranslation();

    // SelectionModel to show selection on data-grid
    const selectionModel: DataGridSelectionModelV1<Alert> = useMemo(
        () => ({
            rowKeyValues: selected.map((a: Alert) => a.id),
            rowKeyValueMap: new Map(selected.map((alert) => [alert.id, alert])),
        }),
        [selected]
    );

    const alertColumns: DataGridColumnV1<Alert>[] = useMemo(() => {
        return [
            {
                key: "name",
                dataKey: "name",
                header: t("label.alert-name"),
                minWidth: 0,
                flex: 1.5,
                sortable: true,
            },
            {
                key: "created",
                dataKey: "created",
                header: t("label.created"),
                minWidth: 0,
                flex: 1,
                sortable: true,
                customCellRenderer: (d: Record<number, unknown>) =>
                    formatDateAndTimeV1(Number(d)),
            },
            {
                key: "updated",
                dataKey: "updated",
                header: t("label.last-updated"),
                minWidth: 0,
                flex: 1,
                sortable: true,
                customCellRenderer: (d: Record<number, unknown>) =>
                    formatDateAndTimeV1(Number(d)),
            },
        ];
    }, []);

    const handleSelectionChange = (
        newSelectedAlerts: DataGridSelectionModelV1<Alert>
    ): void => {
        onSelectionChange(
            Array.from(newSelectedAlerts?.rowKeyValueMap?.values() || [])
        );
    };

    return (
        <DataGridV1<Alert>
            columns={alertColumns}
            data={alertsData as Alert[]}
            data-testid={ANOMALY_FILTERS_TEST_IDS.ALERTS_TABLE}
            initialSortState={{
                key: "name",
                order: DataGridSortOrderV1.ASC,
            }}
            rowKey="id"
            scroll={DataGridScrollV1.Contents}
            searchPlaceholder={t("label.search-entity", {
                entity: t("label.alerts"),
            })}
            selectionModel={selectionModel}
            toolbarComponent={
                <>
                    <span>
                        {alertsData?.length} {t("label.alerts")}
                    </span>
                    <span>|</span>
                    <span>
                        {selected?.length} {t("label.selected")}
                    </span>
                </>
            }
            onSelectionChange={handleSelectionChange}
        />
    );
};
