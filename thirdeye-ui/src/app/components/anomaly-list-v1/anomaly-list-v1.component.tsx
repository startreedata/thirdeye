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
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
} from "../../platform/components";
import { linkRendererV1 } from "../../platform/utils";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    getAlertsAlertPath,
    getAnomaliesAnomalyPath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import { AnomalyQuickFilters } from "../anomaly-quick-filters/anomaly-quick-filters.component";
import { AnomalyListV1Props } from "./anomaly-list-v1.interfaces";

export const AnomalyListV1: FunctionComponent<AnomalyListV1Props> = ({
    anomalies,
    onDelete,
    searchFilterValue,
    onSearchFilterValueChange,
    toolbar = <AnomalyQuickFilters />,
}) => {
    const [selectedAnomaly, setSelectedAnomaly] =
        useState<DataGridSelectionModelV1<UiAnomaly>>();
    const { t } = useTranslation();
    const theme = useTheme();

    const anomalyNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return linkRendererV1(cellValue, getAnomaliesAnomalyPath(data.id));
        },
        []
    );

    const alertNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return linkRendererV1(cellValue, getAlertsAlertPath(data.alertId));
        },
        []
    );

    const metricNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            // currently we don't have id with us3
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
                key: "datasetName",
                dataKey: "datasetName",
                header: t("label.dataset"),
                sortable: true,
                minWidth: 180,
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
                <Box display="flex">
                    <Button
                        data-testid="button-delete"
                        disabled={isActionButtonDisable}
                        variant="contained"
                        onClick={handleAnomalyDelete}
                    >
                        {t("label.delete")}
                    </Button>
                    {toolbar}
                </Box>
            }
            onSearchFilterValueChange={onSearchFilterValueChange}
            onSelectionChange={setSelectedAnomaly}
        />
    );
};
