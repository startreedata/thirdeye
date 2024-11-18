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
import { Button, Card, CardContent, Grid, TextField } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { generateNameForDetectionResult } from "../../../../utils/enumeration-items/enumeration-items.util";
import { EnumerationItemRow } from "./enumeration-item-row/enumeration-item-row.component";
import { EnumerationItemsTableProps } from "./enumeration-items-table.interfaces";
import { useTranslation } from "react-i18next";

export const EnumerationItemsTable: FunctionComponent<EnumerationItemsTableProps> =
    ({
        detectionEvaluations,
        onDeleteClick,
        timezone,
        hideTime,
        showOnlyActivity,
        hideDelete,
        alert,
        evaluationTimeRange,
        legendsPlacement,
        showDimensionSearch,
    }) => {
        const { t } = useTranslation();
        const [searchTerm, setSearchTerm] = useState("");
        const [filteredDetectionEvaluations, setFilteredDetectionEvaluations] =
            useState(detectionEvaluations);
        const handleSearch = (): void => {
            const updated = detectionEvaluations.filter(
                (detectionEvaluation) => {
                    return detectionEvaluation.enumerationItem?.name
                        .toLowerCase()
                        .includes(searchTerm.toLowerCase());
                }
            );
            setFilteredDetectionEvaluations(updated);
        };

        return (
            <Card variant="outlined">
                <CardContent>
                    <Grid container>
                        {showDimensionSearch && (
                            <Grid item sm={12}>
                                <Grid container>
                                    <Grid item sm={6}>
                                        <TextField
                                            fullWidth
                                            placeholder="Search dimensions"
                                            value={searchTerm}
                                            onChange={(e) => {
                                                setSearchTerm(e.target.value);
                                            }}
                                        />
                                    </Grid>
                                    <Grid item alignContent="center">
                                        <Button
                                            color="primary"
                                            variant="contained"
                                            onClick={handleSearch}
                                        >
                                            {t("label.search")}
                                        </Button>
                                    </Grid>
                                </Grid>
                            </Grid>
                        )}
                        <Grid item>
                            {filteredDetectionEvaluations.map(
                                (detectionEvaluation) => {
                                    return (
                                        <EnumerationItemRow
                                            alert={alert}
                                            anomalies={
                                                detectionEvaluation.anomalies
                                            }
                                            detectionEvaluation={
                                                detectionEvaluation
                                            }
                                            evaluationTimeRange={
                                                evaluationTimeRange
                                            }
                                            hideDelete={hideDelete}
                                            hideTime={hideTime}
                                            key={generateNameForDetectionResult(
                                                detectionEvaluation
                                            )}
                                            legendsPlacement={legendsPlacement}
                                            showOnlyActivity={showOnlyActivity}
                                            timezone={timezone}
                                            onDeleteClick={() =>
                                                onDeleteClick(
                                                    detectionEvaluation
                                                )
                                            }
                                        />
                                    );
                                }
                            )}
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>
        );
    };
