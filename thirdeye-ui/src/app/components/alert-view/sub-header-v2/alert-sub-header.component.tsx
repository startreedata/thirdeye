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
import React, { FunctionComponent } from "react";
import { getAlertInsight } from "../../../rest/alerts/alerts.rest";
import { useFetchQuery } from "../../../rest/hooks/useFetchQuery";
import { determineTimezoneFromAlertInEvaluation } from "../../../utils/alerts/alerts.util";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { AlertViewSubHeaderProps } from "./alert-sub-header.interfaces";

export const AlertViewSubHeader: FunctionComponent<AlertViewSubHeaderProps> = ({
    alert,
}) => {
    const getAlertInsightQuery = useFetchQuery({
        queryKey: ["alertInsight", alert.id],
        queryFn: () => {
            return getAlertInsight({ alertId: alert.id });
        },
    });

    return (
        <Grid container justifyContent="space-between">
            <Grid item>
                <Grid container alignItems="center" direction="row">
                    <Grid item>
                        <TimeRangeButtonWithContext
                            btnGroupColor="primary"
                            maxDate={getAlertInsightQuery.data?.datasetEndTime}
                            minDate={
                                getAlertInsightQuery.data?.datasetStartTime
                            }
                            timezone={determineTimezoneFromAlertInEvaluation(
                                getAlertInsightQuery.data
                                    ?.templateWithProperties
                            )}
                        />
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};
