import { Box, Grid } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { AnomalyQuickFilters } from "../../components/anomaly-quick-filters/anomaly-quick-filters.component";
import { MetricsReportList } from "../../components/metrics-report-list/metrics-report-list.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { PageContentsCardV1 } from "../../platform/components";
import { AnomaliesAllPageContext } from "../anomalies-all-page/anomalies-all-page.interfaces";

export const MetricsReportAllPage: FunctionComponent = () => {
    const { anomalies } = useOutletContext<AnomaliesAllPageContext>();
    const [searchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <Box padding="8px">
                    <AnomalyQuickFilters />
                </Box>
                <MetricsReportList
                    anomalies={anomalies}
                    chartEnd={endTime}
                    chartStart={startTime}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
