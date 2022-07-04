/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Button, Typography } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridSelectionModelV1,
    DataGridV1,
} from "../../platform/components";
import { linkRendererV1 } from "../../platform/utils";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    getAlertsViewPath,
    getAnomaliesAnomalyPath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import { AnomalyQuickFilters } from "../anomaly-quick-filters/anomaly-quick-filters.component";
import { AnomalyListV1Props } from "./anomaly-list-v1.interfaces";

export const AnomalyListV1: FunctionComponent<AnomalyListV1Props> = (
    props: AnomalyListV1Props
) => {
    const [selectedAnomaly, setSelectedAnomaly] =
        useState<DataGridSelectionModelV1<UiAnomaly>>();
    const { t } = useTranslation();

    const anomalyNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return linkRendererV1(cellValue, getAnomaliesAnomalyPath(data.id));
        },
        []
    );

    const alertNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return linkRendererV1(cellValue, getAlertsViewPath(data.alertId));
        },
        []
    );

    const metricNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            // currently we don't have id with us
            // but it will be good to have id to redirect
            return data.metricId
                ? linkRendererV1(cellValue, getMetricsViewPath(data.metricId))
                : cellValue;
        },
        []
    );

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

    const durationRenderer = useCallback(
        // use formatted value to display
        (_, data: UiAnomaly) => data.duration,
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

    const isActionButtonDisable = !(
        selectedAnomaly && selectedAnomaly.rowKeyValues.length === 1
    );

    const handleAnomalyDelete = (): void => {
        if (!selectedAnomaly) {
            return;
        }

        const anomalyId = selectedAnomaly.rowKeyValues[0];
        const anomaly = selectedAnomaly.rowKeyValueMap?.get(anomalyId);

        props.onDelete && props.onDelete(anomaly as UiAnomaly);
    };

    const anomalyListColumns = useMemo(
        () => [
            {
                key: "name",
                dataKey: "name",
                header: t("label.name"),
                sortable: true,
                minWidth: 200,
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
        ],
        [
            anomalyNameRenderer,
            alertNameRenderer,
            deviationRenderer,
            currentRenderer,
            predicatedRenderer,
            durationRenderer,
            startTimeRenderer,
            endTimeRenderer,
            metricNameRenderer,
        ]
    );

    return (
        <DataGridV1<UiAnomaly>
            hideBorder
            columns={anomalyListColumns}
            data={props.anomalies as UiAnomaly[]}
            rowKey="id"
            searchFilterValue={props.searchFilterValue}
            searchPlaceholder={t("label.search-entity", {
                entity: t("label.anomalies"),
            })}
            toolbarComponent={
                <Box display="flex" justifyContent="space-between">
                    <Button
                        data-testid="button-delete"
                        disabled={isActionButtonDisable}
                        variant="contained"
                        onClick={handleAnomalyDelete}
                    >
                        {t("label.delete")}
                    </Button>
                    <AnomalyQuickFilters />
                </Box>
            }
            onSearchFilterValueChange={props.onSearchFilterValueChange}
            onSelectionChange={setSelectedAnomaly}
        />
    );
};
