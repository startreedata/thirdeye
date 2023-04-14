/*
 * Copyright 2023 StarTree Inc
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
import { Grid } from "@material-ui/core";
import { parse, toSeconds } from "iso8601-duration";
import React, { FunctionComponent, useEffect } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { PageContentsCardV1, SkeletonV1 } from "../../platform/components";
import { getTimePaddingForGranularity } from "../../utils/anomalies/anomalies.util";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { AnomalyViewContainerPageOutletContext } from "../anomalies-view-page/anomalies-view-page.interfaces";

/**
 * The sole purpose of this page is to figure out the default dates
 * to redirect to the anomalies view page to
 */
export const AnomaliesViewIndexPage: FunctionComponent = () => {
    const { anomaly, alertInsight } =
        useOutletContext<AnomalyViewContainerPageOutletContext>();
    const navigate = useNavigate();

    useEffect(() => {
        if (anomaly && alertInsight) {
            // Default to 2 weeks and for days
            const timeDiff =
                toSeconds(
                    parse(
                        getTimePaddingForGranularity(
                            alertInsight.templateWithProperties?.metadata
                                ?.granularity
                        )
                    )
                ) * 1000;

            const timeRangeQuery = new URLSearchParams([
                [TimeRangeQueryStringKey.TIME_RANGE, TimeRange.CUSTOM],
                [
                    TimeRangeQueryStringKey.START_TIME,
                    (anomaly.startTime - timeDiff).toString(),
                ],
                [
                    TimeRangeQueryStringKey.END_TIME,
                    (anomaly.endTime + timeDiff).toString(),
                ],
            ]);

            navigate(
                `${
                    AppRouteRelative.ANOMALIES_ANOMALY_VIEW_VALIDATE
                }?${timeRangeQuery.toString()}`,
                {
                    replace: true,
                }
            );
        }
    }, [anomaly, alertInsight]);

    return (
        <>
            {/** chart section */}
            <Grid item xs={12}>
                <PageContentsCardV1>
                    <SkeletonV1 height={50} />
                    <SkeletonV1 height={370} />
                </PageContentsCardV1>
            </Grid>
        </>
    );
};
