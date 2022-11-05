import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
} from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    AnomaliesByAlert,
    MetricsReportListProps,
} from "./metrics-report-list.interfaces";
import { useMetricsReportListStyles } from "./metrics-report-list.styles";
import { categorizeAnomalies } from "./metrics-report-list.utils";
import { MetricsReportRow } from "./metrics-report-row/metrics-report-row.component";

export const MetricsReportList: FunctionComponent<MetricsReportListProps> = ({
    anomalies,
    chartStart,
    chartEnd,
}) => {
    const { t } = useTranslation();
    const classes = useMetricsReportListStyles();
    const [anomaliesByAlert, setAnomaliesByAlert] = useState<
        AnomaliesByAlert[]
    >(anomalies ? categorizeAnomalies(anomalies) : []);

    useEffect(() => {
        if (anomalies) {
            setAnomaliesByAlert(categorizeAnomalies(anomalies));
        }
    }, [anomalies]);

    return (
        <Table>
            <TableHead>
                <TableRow className={classes.tableHeader}>
                    <TableCell>{t("label.alert")}</TableCell>
                    <TableCell>{t("label.metric")}</TableCell>
                    <TableCell>{t("label.dataset")}</TableCell>
                    <TableCell>{t("label.chart")}</TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {anomaliesByAlert.map((row) => (
                    <MetricsReportRow
                        anomalyAlert={row}
                        chartEnd={chartEnd}
                        chartStart={chartStart}
                        key={row.alert.name}
                    />
                ))}
            </TableBody>
        </Table>
    );
};
