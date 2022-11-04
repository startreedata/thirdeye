// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
