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

// state
import { useCreateAlertStore } from "../../hooks/state";

// sections
import { SelectAggregation } from "./aggregation";
import { SelectGranularity } from "./granularity";
import { SelectDetection } from "./detection";

// app components
import { ColumnsDrawer } from "../../../../components/columns-drawer/columns-drawer.component";

export const AlertProperties = (): JSX.Element => {
    const {
        selectedMetric,
        selectedDataset,
        viewColumnsListDrawer,
        setViewColumnsListDrawer,
    } = useCreateAlertStore();

    return (
        <Grid item xs={12}>
            <Grid container>
                {selectedMetric && (
                    <>
                        <SelectAggregation />
                        <SelectGranularity />
                        <SelectDetection />
                        {selectedDataset && (
                            <ColumnsDrawer
                                datasetId={selectedDataset?.dataset.id}
                                isOpen={viewColumnsListDrawer}
                                selectedDataset={selectedDataset}
                                onClose={() =>
                                    setViewColumnsListDrawer(
                                        !viewColumnsListDrawer
                                    )
                                }
                            />
                        )}
                    </>
                )}
            </Grid>
        </Grid>
    );
};
