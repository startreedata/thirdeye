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
    DataGridScrollV1,
    DataGridSortOrderV1,
    DataGridV1,
} from "../../../platform/components";
import { linkRendererV1 } from "../../../platform/utils";
import { UiAnomaly } from "../../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../../utils/anomalies/anomalies.util";
import { getAnomaliesAnomalyPath } from "../../../utils/routes/routes.util";
import { MetricReportExpandListProps } from "./metrics-report-expand-list.interface";

export const MetricReportExpandList: FunctionComponent<MetricReportExpandListProps> =
    ({ anomalies }) => {
        const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(
            getUiAnomalies(anomalies)
        );
        const { t } = useTranslation();

        useEffect(() => {
            if (anomalies && anomalies.length > 0) {
                setUiAnomalies(getUiAnomalies(anomalies));
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

        const metricsReportExpandColumns = useMemo(
            () => [
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
            ],
            [
                deviationRenderer,
                currentRenderer,
                predicatedRenderer,
                startTimeRenderer,
                endTimeRenderer,
            ]
        );

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
