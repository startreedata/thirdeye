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
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { TimeRangeQueryStringKey } from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AppLoadingIndicatorV1 } from "../../../platform/components";
import { baselineOffsetToMilliseconds } from "../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { getRootCauseAnalysisForAnomalyInvestigateV2Path } from "../../../utils/routes/routes.util";
import { WEEK_IN_MILLISECONDS } from "../../../utils/time/time.util";
import { InvestigationContext } from "../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";

export const IndexPage: FunctionComponent = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { anomaly, alertInsight } = useOutletContext<InvestigationContext>();

    useEffect(() => {
        if (anomaly && alertInsight) {
            let startTime = anomaly.startTime - WEEK_IN_MILLISECONDS * 2;
            let endTime = anomaly.endTime + WEEK_IN_MILLISECONDS * 2;

            if (alertInsight?.templateWithProperties?.metadata?.granularity) {
                const granularity =
                    alertInsight?.templateWithProperties?.metadata?.granularity;
                const padding = granularity
                    ? baselineOffsetToMilliseconds(granularity) * 14
                    : 0;

                startTime = Number(anomaly.startTime) - padding;
                endTime = Number(anomaly.endTime) + padding;
            }

            searchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                startTime.toString()
            );
            searchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                endTime.toString()
            );

            navigate(
                `${getRootCauseAnalysisForAnomalyInvestigateV2Path(
                    toNumber(anomaly.id)
                )}?${searchParams.toString()}`,
                {
                    replace: true,
                }
            );
        }
    }, [anomaly, alertInsight]);

    return <AppLoadingIndicatorV1 />;
};
