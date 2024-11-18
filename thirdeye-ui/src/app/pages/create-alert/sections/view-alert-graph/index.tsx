/*
 * Copyright 2024 StarTree Inc
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
// external
import React from "react";
import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";

// sections
import { GraphOptions } from "./graph-options";
import { GraphPlot } from "./graph-plot";

// state
import { useCreateAlertStore } from "../../hooks/state";

// types
import { AnomalyDetectionOptions } from "../../../../rest/dto/metric.interfaces";

export const ViewAlertGraph = (): JSX.Element => {
    const {
        aggregationFunction,
        editedDatasourceFieldValue,
        granularity,
        anomalyDetectionType,
        selectedEnumerationItems,
        enumeratorQuery,
        workingAlertEvaluation,
        selectedDetectionAlgorithm,
    } = useCreateAlertStore();
    let showGraph = false;
    if (anomalyDetectionType === AnomalyDetectionOptions.SINGLE) {
        showGraph = true;
    } else {
        if (!isEmpty(selectedEnumerationItems)) {
            showGraph = true;
        }
        if (enumeratorQuery) {
            showGraph =
                !!workingAlertEvaluation || !!selectedDetectionAlgorithm;
        }
    }
    showGraph =
        showGraph &&
        (!!aggregationFunction || !!editedDatasourceFieldValue) &&
        !!granularity;

    return (
        <Grid item xs={12}>
            {showGraph && (
                <>
                    <GraphOptions />
                    <GraphPlot />
                </>
            )}
        </Grid>
    );
};
