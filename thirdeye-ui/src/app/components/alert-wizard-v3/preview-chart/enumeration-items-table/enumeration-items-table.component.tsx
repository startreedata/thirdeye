/*
 * Copyright 2022 StarTree Inc
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
import { Card, CardContent, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { generateNameForDetectionResult } from "../../../../utils/enumeration-items/enumeration-items.util";
import { EnumerationItemRow } from "./enumeration-item-row/enumeration-item-row.component";
import { EnumerationItemsTableProps } from "./enumeration-items-table.interfaces";

export const EnumerationItemsTable: FunctionComponent<EnumerationItemsTableProps> =
    ({ detectionEvaluations, onDeleteClick }) => {
        return (
            <Card variant="outlined">
                <CardContent>
                    <Grid container>
                        {detectionEvaluations.map((detectionEvaluation) => {
                            return (
                                <EnumerationItemRow
                                    anomalies={detectionEvaluation.anomalies}
                                    detectionEvaluation={detectionEvaluation}
                                    key={generateNameForDetectionResult(
                                        detectionEvaluation
                                    )}
                                    onDeleteClick={() =>
                                        onDeleteClick(detectionEvaluation)
                                    }
                                />
                            );
                        })}
                    </Grid>
                </CardContent>
            </Card>
        );
    };
