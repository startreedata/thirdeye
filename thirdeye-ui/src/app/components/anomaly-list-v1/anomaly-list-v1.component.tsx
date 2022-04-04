import { Button, Typography } from "@material-ui/core";
import React, { FunctionComponent, ReactNode, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
} from "../../platform/components";
import {
    formatDateAndTimeV1,
    formatDurationV1,
    formatLargeNumberV1,
    formatPercentageV1,
    linkRendererV1,
    numberSortComparatorV1,
} from "../../platform/utils";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { getAnomalyName } from "../../utils/anomalies/anomalies.util";
import {
    getAlertsViewPath,
    getAnomaliesAnomalyPath,
} from "../../utils/routes/routes.util";
import { AnomalyListV1Props } from "./anomaly-list-v1.interfaces";

export const AnomalyListV1: FunctionComponent<AnomalyListV1Props> = (
    props: AnomalyListV1Props
) => {
    const [selectedAnomaly, setSelectedAnomaly] =
        useState<DataGridSelectionModelV1<Anomaly>>();
    const { t } = useTranslation();

    const anomalyNameRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode =>
        linkRendererV1(getAnomalyName(data), getAnomaliesAnomalyPath(data.id));

    const alertNameRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode =>
        linkRendererV1(data.alert.name, getAlertsViewPath(data.alert.id));

    const durationRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode => formatDurationV1(data.startTime, data.endTime);

    const startTimeRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode => formatDateAndTimeV1(data.startTime);

    const endTimeRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode => formatDateAndTimeV1(data.endTime);

    const currentRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode => formatLargeNumberV1(data.avgCurrentVal);

    const predictedRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode => formatLargeNumberV1(data.avgBaselineVal);

    const deviationRenderer = (
        _cellValue: Record<string, unknown>,
        data: Anomaly
    ): ReactNode => {
        const deviationVal =
            (data.avgCurrentVal - data.avgBaselineVal) / data.avgBaselineVal;

        return (
            <Typography
                color={deviationVal < 0 ? "error" : undefined}
                variant="body2"
            >
                {formatPercentageV1(deviationVal)}
            </Typography>
        );
    };

    const isActionButtonDisable = !(
        selectedAnomaly && selectedAnomaly.rowKeyValues.length === 1
    );

    const handleAnomalyDelete = (): void => {
        if (!selectedAnomaly) {
            return;
        }

        const anomalyId = selectedAnomaly.rowKeyValues[0];
        const anomaly = selectedAnomaly.rowKeyValueMap?.get(anomalyId);

        props.onDelete && props.onDelete(anomaly as Anomaly);
    };

    const sortDeviation = (
        data1: Anomaly,
        data2: Anomaly,
        order: DataGridSortOrderV1
    ): number => {
        const val1 = !isNaN(
            (data1.avgCurrentVal - data1.avgBaselineVal) / data1.avgBaselineVal
        )
            ? (data1.avgCurrentVal - data1.avgBaselineVal) /
              data1.avgBaselineVal
            : 0;
        const val2 = !isNaN(
            (data2.avgCurrentVal - data2.avgBaselineVal) / data2.avgBaselineVal
        )
            ? (data2.avgCurrentVal - data2.avgBaselineVal) /
              data2.avgBaselineVal
            : 0;

        return numberSortComparatorV1(val1, val2, order);
    };

    const sortDuration = (
        data1: Anomaly,
        data2: Anomaly,
        order: DataGridSortOrderV1
    ): number => {
        const duration1 = data1.endTime - data1.startTime;
        const duration2 = data2.endTime - data2.startTime;

        return numberSortComparatorV1(duration1, duration2, order);
    };

    const anomalyListColumns = useMemo(
        () => [
            {
                key: "id",
                dataKey: "id",
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
                key: "duration",
                dataKey: "duration",
                header: t("label.duration"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: durationRenderer,
                sortComparatorFn: sortDuration,
            },
            {
                key: "startTime",
                dataKey: "startTime",
                header: t("label.start"),
                sortable: true,
                minWidth: 200,
                customCellRenderer: startTimeRenderer,
            },
            {
                key: "endTime",
                dataKey: "endTime",
                header: t("label.end"),
                sortable: true,
                minWidth: 200,
                customCellRenderer: endTimeRenderer,
            },
            {
                key: "current",
                dataKey: "avgCurrentVal",
                header: t("label.current"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: currentRenderer,
            },
            {
                key: "predicted",
                dataKey: "avgBaselineVal",
                header: t("label.predicted"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: predictedRenderer,
            },
            {
                key: "deviation",
                dataKey: "avgCurrentVal",
                header: t("label.deviation"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: deviationRenderer,
                sortComparatorFn: sortDeviation,
            },
        ],
        [anomalyNameRenderer, alertNameRenderer, deviationRenderer]
    );

    return (
        <DataGridV1<Anomaly>
            hideBorder
            columns={anomalyListColumns}
            data={props.anomalies}
            rowKey="id"
            searchFilterValue={props.searchFilterValue}
            searchPlaceholder={t("label.search-entity", {
                entity: t("label.anomalies"),
            })}
            toolbarComponent={
                <Button
                    data-testid="button-delete"
                    disabled={isActionButtonDisable}
                    variant="contained"
                    onClick={handleAnomalyDelete}
                >
                    {t("label.delete")}
                </Button>
            }
            onSearchFilterValueChange={props.onSearchFilterValueChange}
            onSelectionChange={setSelectedAnomaly}
        />
    );
};
