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
import { Typography } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridColumnV1,
    DataGridScrollV1,
    DataGridSortOrderV1,
    DataGridV1,
    SkeletonV1,
} from "../../../platform/components";
import { linkRendererV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import { UiAnomaly } from "../../../rest/dto/ui-anomaly.interfaces";
import { useGetEnumerationItems } from "../../../rest/enumeration-items/enumeration-items.actions";
import { getUiAnomalies } from "../../../utils/anomalies/anomalies.util";
import { getAnomaliesAnomalyPath } from "../../../utils/routes/routes.util";
import { MetricReportExpandListProps } from "./metrics-report-expand-list.interface";

export const MetricReportExpandList: FunctionComponent<MetricReportExpandListProps> =
    ({ anomalies, showEnumerationItem }) => {
        const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(
            getUiAnomalies(anomalies)
        );
        const { t } = useTranslation();
        const {
            enumerationItems,
            getEnumerationItems,
            status: enumerationItemsStatus,
        } = useGetEnumerationItems();

        useEffect(() => {
            if (anomalies && anomalies.length > 0) {
                setUiAnomalies(getUiAnomalies(anomalies));

                if (showEnumerationItem) {
                    const hasEnumerationList = anomalies.filter(
                        (a) => a.enumerationItem
                    );

                    if (hasEnumerationList.length > 0) {
                        const ids = Array.from(
                            new Set(
                                hasEnumerationList.map(
                                    (a) => a.enumerationItem?.id as number
                                )
                            )
                        );

                        getEnumerationItems({
                            ids,
                        });
                    }
                }
            } else {
                setUiAnomalies([]);
            }
        }, [anomalies]);

        const deviationRenderer = useCallback(
            (_: Record<string, unknown>, data: UiAnomaly): ReactNode => {
                return (
                    <Typography
                        color={data.negativeDeviation ? "error" : undefined}
                        variant="body2"
                    >
                        {data.deviation}
                    </Typography>
                );
            },
            []
        );

        const currentRenderer = useCallback(
            (_: Record<string, unknown>, data: UiAnomaly): ReactNode =>
                // use formatted value to display
                data.current,
            []
        );

        const predicatedRenderer = useCallback(
            (_: Record<string, unknown>, data: UiAnomaly): ReactNode =>
                // use formatted value to display
                data.predicted,
            []
        );

        const startTimeRenderer = useCallback(
            // use formatted value to display
            (_, data: UiAnomaly) => data.startTime,
            []
        );

        const endTimeRenderer = useCallback(
            // use formatted value to display
            (_, data: UiAnomaly) => data.endTime,
            []
        );

        const anomalyIdRenderer = useCallback(
            (
                cellValue: Record<string, unknown>,
                data: UiAnomaly
            ): ReactNode => {
                return linkRendererV1(
                    cellValue,
                    getAnomaliesAnomalyPath(data.id)
                );
            },
            []
        );

        const enumerationItemMap = useMemo<Record<number, EnumerationItem>>(
            () =>
                showEnumerationItem && enumerationItems
                    ? Object.assign(
                          {},
                          ...(enumerationItems || [])?.map((e) => ({
                              [Number(e.id)]: e,
                          }))
                      )
                    : {},
            [showEnumerationItem, enumerationItems]
        );

        const enumerationItemRender = useCallback(
            // use formatted value to display
            (_, data: UiAnomaly) => {
                if (!(showEnumerationItem && data.enumerationId)) {
                    return t("message.no-data");
                }

                if (
                    enumerationItemsStatus &&
                    [ActionStatus.Initial, ActionStatus.Working].includes(
                        enumerationItemsStatus
                    )
                ) {
                    return <SkeletonV1 width={150} />;
                }

                if (
                    enumerationItemMap &&
                    data.enumerationId in enumerationItemMap &&
                    enumerationItemMap[data?.enumerationId]?.name
                ) {
                    return enumerationItemMap[data?.enumerationId]?.name;
                }

                return t("message.no-data");
            },

            [enumerationItemMap, enumerationItemsStatus]
        );

        const enumerationItemTooltip = useCallback(
            (_, data: UiAnomaly) => {
                if (!(showEnumerationItem && data.enumerationId)) {
                    return t("message.no-data");
                }

                if (
                    enumerationItemMap &&
                    data.enumerationId in enumerationItemMap &&
                    enumerationItemMap[data.enumerationId].name
                ) {
                    const tooltipParams =
                        enumerationItemMap[data?.enumerationId]?.params;

                    return Object.entries(tooltipParams)
                        .map(([k, v]) => (
                            <>
                                {k}: &quot;{v.toString().trim()}&quot;
                            </>
                        ))
                        .reduce((sum, val) => (
                            <>
                                {sum}
                                <br />
                                {val}
                            </>
                        ));
                }

                return t("message.no-data");
            },
            [enumerationItemMap, enumerationItemsStatus]
        );

        const metricsReportExpandColumns = useMemo(() => {
            const columns: DataGridColumnV1<UiAnomaly>[] = [
                {
                    key: "name",
                    dataKey: "name",
                    header: t("label.name"),
                    sortable: true,
                    minWidth: 200,
                    customCellRenderer: anomalyIdRenderer,
                },
                {
                    key: "startTime",
                    dataKey: "startTimeVal",
                    header: t("label.start"),
                    sortable: true,
                    minWidth: 200,
                    customCellRenderer: startTimeRenderer,
                },
                {
                    key: "endTime",
                    dataKey: "endTimeVal",
                    header: t("label.end"),
                    sortable: true,
                    minWidth: 200,
                    customCellRenderer: endTimeRenderer,
                },
                {
                    key: "deviation",
                    dataKey: "deviationVal",
                    header: t("label.deviation"),
                    sortable: true,
                    minWidth: 150,
                    customCellRenderer: deviationRenderer,
                },
                {
                    key: "current",
                    dataKey: "currentVal",
                    header: t("label.current"),
                    sortable: true,
                    minWidth: 150,
                    customCellRenderer: currentRenderer,
                },
                {
                    key: "predicted",
                    dataKey: "predictedVal",
                    header: t("label.predicted"),
                    sortable: true,
                    minWidth: 150,
                    customCellRenderer: predicatedRenderer,
                },
            ];

            if (showEnumerationItem) {
                columns.push({
                    key: "enumerationItem",
                    dataKey: "enumerationItem",
                    header: t("label.enumeration-item"),
                    sortable: true,
                    minWidth: 300,
                    customCellRenderer: enumerationItemRender,
                    customCellTooltipRenderer: enumerationItemTooltip,
                });
            }

            return columns;
        }, [
            deviationRenderer,
            currentRenderer,
            predicatedRenderer,
            startTimeRenderer,
            endTimeRenderer,
            showEnumerationItem,
            enumerationItemRender,
            enumerationItemTooltip,
        ]);

        return (
            <DataGridV1<UiAnomaly>
                disableSearch
                disableSelection
                hideBorder
                hideToolbar
                columns={metricsReportExpandColumns}
                data={uiAnomalies}
                initialSortState={{
                    key: "startTime",
                    order: DataGridSortOrderV1.DESC,
                }}
                rowKey="id"
                scroll={DataGridScrollV1.Body}
            />
        );
    };
