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
import { Box, Button, Typography, useTheme } from "@material-ui/core";
import CheckCircleOutlineIcon from "@material-ui/icons/CheckCircleOutline";
import HighlightOffIcon from "@material-ui/icons/HighlightOff";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridColumnV1,
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
    SkeletonV1,
} from "../../platform/components";
import { formatDateAndTimeV1, linkRendererV1 } from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";
import type { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    getAlertsAlertPath,
    getAnomaliesAnomalyPath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import { AnomalyQuickFilters } from "../anomaly-quick-filters/anomaly-quick-filters.component";
import type { AnomalyListV1Props } from "./anomaly-list-v1.interfaces";
import { useAnomalyListV1Styles } from "./anomaly-list-v1.style";

export const AnomalyListV1: FunctionComponent<AnomalyListV1Props> = ({
    anomalies,
    onDelete,
    searchFilterValue,
    onSearchFilterValueChange,
    toolbar = <AnomalyQuickFilters />,
    showEnumerationItem = false,
    enumerationItems = [],
    enumerationItemsStatus,
    // If timezone is passed, override all datetime rendering to use this timezone
    timezone,
}) => {
    const [selectedAnomaly, setSelectedAnomaly] =
        useState<DataGridSelectionModelV1<UiAnomaly>>();
    const { t } = useTranslation();
    const theme = useTheme();
    const styles = useAnomalyListV1Styles();

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

    const addMutedStyle = useCallback(
        (content: ReactNode, data: UiAnomaly): ReactNode => {
            const { isIgnored } = data;

            return (
                <Typography
                    variant="body2"
                    {...(isIgnored && { className: styles.muted })}
                >
                    {content}
                </Typography>
            );
        },
        []
    );

    const anomalyNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            const { isIgnored, id } = data;

            return addMutedStyle(
                linkRendererV1(
                    `${t("label.entity-id", { id: cellValue })}${
                        isIgnored ? `(${t("label.ignored")})` : ""
                    }`,
                    getAnomaliesAnomalyPath(id),
                    false,
                    `${t("label.view")} ${data.name}`
                ),
                data
            );
        },
        []
    );

    const alertNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return addMutedStyle(
                linkRendererV1(
                    cellValue,
                    getAlertsAlertPath(data.alertId),
                    false,
                    `${t("label.view")} ${t("label.alert")}:${cellValue}`
                ),
                data
            );
        },
        []
    );

    const metricNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            // currently we don't have id with us3
            // but it will be good to have id to redirect
            return addMutedStyle(
                data.metricId
                    ? linkRendererV1(
                          cellValue,
                          getMetricsViewPath(data.metricId)
                      )
                    : cellValue,
                data
            );
        },
        []
    );

    const datasetRenderer = useCallback(
        // use formatted value to display
        (_, data: UiAnomaly) => addMutedStyle(data.datasetName, data),
        []
    );

    const deviationRenderer = useCallback(
        (_: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return addMutedStyle(
                <Typography
                    color={data.negativeDeviation ? "error" : undefined}
                    variant="body2"
                >
                    {data.deviation}
                </Typography>,
                data
            );
        },
        []
    );
    const currentRenderer = useCallback(
        (_: Record<string, unknown>, data: UiAnomaly): ReactNode =>
            // use formatted value to display
            addMutedStyle(data.current, data),
        []
    );

    const predicatedRenderer = useCallback(
        (_: Record<string, unknown>, data: UiAnomaly): ReactNode =>
            // use formatted value to display
            addMutedStyle(data.predicted, data),
        []
    );

    const durationRenderer = useCallback(
        // use formatted value to display
        (_, data: UiAnomaly) => addMutedStyle(data.duration, data),
        []
    );

    const startTimeRenderer = useCallback(
        // use formatted value to display
        (_, data: UiAnomaly) =>
            addMutedStyle(
                formatDateAndTimeV1(data.startTimeVal, timezone),
                data
            ),
        [timezone]
    );

    const endTimeRenderer = useCallback(
        // use formatted value to display
        (_, data: UiAnomaly) =>
            addMutedStyle(formatDateAndTimeV1(data.endTimeVal, timezone), data),
        [timezone]
    );

    const enumerationItemRender = useCallback(
        // use formatted value to display
        (_, data: UiAnomaly) => {
            const returnParsed = (v?: ReactNode): ReactNode =>
                addMutedStyle(v || t("message.no-data"), data);

            if (!(showEnumerationItem && data.enumerationId)) {
                return returnParsed();
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
                return returnParsed(
                    enumerationItemMap[data?.enumerationId]?.name
                );
            }

            return returnParsed();
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

    const hasFeedbackRenderer = useCallback(
        // use formatted value to display
        (hasFeedback) => {
            return hasFeedback ? (
                <CheckCircleOutlineIcon
                    htmlColor={theme.palette.success.main}
                />
            ) : (
                <HighlightOffIcon color="secondary" />
            );
        },
        []
    );

    const isActionButtonDisable = !(
        selectedAnomaly && selectedAnomaly.rowKeyValues.length > 0
    );

    const handleAnomalyDelete = (): void => {
        if (!selectedAnomaly || !selectedAnomaly.rowKeyValueMap) {
            return;
        }
        onDelete &&
            onDelete(Array.from(selectedAnomaly.rowKeyValueMap.values()));
    };

    const anomalyListColumns = useMemo<DataGridColumnV1<UiAnomaly>[]>(() => {
        const columns: DataGridColumnV1<UiAnomaly>[] = [
            {
                key: "name",
                dataKey: "id",
                header: t("message.entity-id-verbose-header", {
                    entity: t("label.anomaly"),
                }),
                sortable: true,
                minWidth: 150,
                customCellRenderer: anomalyNameRenderer,
            },
            {
                key: "alertName",
                dataKey: "alertName",
                header: t("label.alert"),
                sortable: true,
                minWidth: 300,
                customCellRenderer: alertNameRenderer,
            },
            {
                key: "metricName",
                dataKey: "metricName",
                header: t("label.metric"),
                sortable: true,
                minWidth: 180,
                customCellRenderer: metricNameRenderer,
            },
            {
                key: "datasetName",
                dataKey: "datasetName",
                header: t("label.dataset"),
                sortable: true,
                minWidth: 180,
                customCellRenderer: datasetRenderer,
            },
            {
                key: "duration",
                dataKey: "durationVal",
                header: t("label.duration"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: durationRenderer,
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
            {
                key: "deviation",
                dataKey: "deviationVal",
                header: t("label.deviation"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: deviationRenderer,
            },
            {
                key: "hasFeedback",
                dataKey: "hasFeedback",
                header: t("label.has-feedback"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: hasFeedbackRenderer,
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
        anomalyNameRenderer,
        alertNameRenderer,
        deviationRenderer,
        currentRenderer,
        predicatedRenderer,
        durationRenderer,
        startTimeRenderer,
        endTimeRenderer,
        metricNameRenderer,
        enumerationItemRender,
        enumerationItemTooltip,
        showEnumerationItem,
    ]);

    return (
        <DataGridV1<UiAnomaly>
            disableSearch
            hideBorder
            columns={anomalyListColumns}
            data={anomalies as UiAnomaly[]}
            initialSortState={{
                key: "startTime",
                order: DataGridSortOrderV1.DESC,
            }}
            rowKey="id"
            searchFilterValue={searchFilterValue}
            toolbarComponent={
                <Box display="flex" gridGap={12}>
                    <Box display="flex">
                        <Button
                            data-testid="button-delete"
                            disabled={isActionButtonDisable}
                            variant="contained"
                            onClick={handleAnomalyDelete}
                        >
                            {t("label.delete")}
                        </Button>
                    </Box>
                    {toolbar}
                </Box>
            }
            onSearchFilterValueChange={onSearchFilterValueChange}
            onSelectionChange={setSelectedAnomaly}
        />
    );
};
