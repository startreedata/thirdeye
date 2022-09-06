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
import { DataGridScrollV1, DataGridV1 } from "../../platform/components";
import { getAnomalies } from "../../rest/anomalies/anomalies.rest";
import { GetAnomaliesProps } from "../../rest/anomalies/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";
import { TimeRangeQueryStringKey } from "../time-range/time-range-provider/time-range-provider.interfaces";
import { MetricReportExpandListProps } from "./metrics-report-expand-list.interface";

export const MetricReportExpandList: FunctionComponent<
    MetricReportExpandListProps
> = ({ id, searchParams }) => {
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);
    const { t } = useTranslation();

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

    useEffect(() => {
        // Time range refreshed, fetch anomalies
        fetchAnomaliesByTime();
    }, [searchParams]);

    const fetchAnomaliesByTime = (): void => {
        setUiAnomalies(null);

        const start =
            searchParams &&
            searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end =
            searchParams && searchParams.get(TimeRangeQueryStringKey.END_TIME);
        const params: GetAnomaliesProps = {
            startTime: Number(start),
            endTime: Number(end),
        };

        params.alertId = id;

        let fetchedUiAnomalies: UiAnomaly[] = [];

        getAnomalies(params)
            .then((anomalies) => {
                if (anomalies && anomalies.length) {
                    fetchedUiAnomalies = getUiAnomalies(anomalies);
                }
            })
            .finally(() => setUiAnomalies(fetchedUiAnomalies));
    };

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

    const metricsReportExpandColumns = useMemo(
        () => [
            {
                key: "id",
                dataKey: "id",
                header: t("label.id"),
                sortable: true,
                minWidth: 200,
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
            rowKey="id"
            scroll={DataGridScrollV1.Body}
        />
    );
};
