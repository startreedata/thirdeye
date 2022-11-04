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
    Box,
    Button,
    Card,
    CardContent,
    Divider,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import { sortBy } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { DataGridSortOrderV1 } from "../../../platform/components";
import {
    filterEvaluations,
    generateNameForDetectionResult,
} from "../../../utils/enumeration-items/enumeration-items.util";
import { Pluralize } from "../../pluralize/pluralize.component";
import { EnumerationItemRow } from "./enumeration-item-row/enumeration-item-row.component";
import { EnumerationItemsTableProps } from "./enumeration-items-table.interfaces";

export const EnumerationItemsTable: FunctionComponent<EnumerationItemsTableProps> =
    ({
        detectionEvaluations,
        expanded,
        onExpandedChange,
        alertId,
        initialSearchTerm,
        onSearchTermChange,
        sortOrder,
        onSortOrderChange,
    }) => {
        const [filteredDetectionEvaluations, setFilteredDetectionEvaluations] =
            useState(
                filterEvaluations(detectionEvaluations, initialSearchTerm)
            );
        const [sortedDetectionEvaluations, setSortedDetectionEvaluations] =
            useState(
                filterEvaluations(detectionEvaluations, initialSearchTerm)
            );
        const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
        const { t } = useTranslation();

        const handleSearchClick = (term: string): void => {
            onSearchTermChange(term);
            if (term === "") {
                setFilteredDetectionEvaluations(detectionEvaluations);

                return;
            }

            setFilteredDetectionEvaluations(
                filterEvaluations(detectionEvaluations, searchTerm)
            );
        };

        const handleIsOpenChange = (isOpen: boolean, name: string): void => {
            let copied = [...expanded];

            if (isOpen) {
                copied.push(name);
            } else {
                copied = copied.filter((c) => c !== name);
            }

            onExpandedChange(copied);
        };

        useEffect(() => {
            let copied = sortBy(
                [...filteredDetectionEvaluations],
                "lastAnomalyTs"
            );

            if (sortOrder === DataGridSortOrderV1.DESC) {
                copied = copied.reverse();
            }

            setSortedDetectionEvaluations(copied);
        }, [sortOrder, filteredDetectionEvaluations]);

        return (
            <Card variant="outlined">
                {detectionEvaluations.length > 1 && (
                    <CardContent>
                        <Grid container>
                            <Grid item xs={12}>
                                <Box paddingBottom={1}>
                                    <Typography variant="h5">
                                        {t("label.alert")}
                                    </Typography>
                                    <Typography variant="body2">
                                        {t(
                                            "message.list-of-all-dimensions-related-to-alert"
                                        )}
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid container item xs={12}>
                                <Grid item sm={2} xs={12}>
                                    Search dimensions
                                </Grid>
                                <Grid item sm={10} xs={12}>
                                    <form
                                        onSubmit={(e) => {
                                            e.preventDefault();
                                            handleSearchClick(searchTerm);
                                        }}
                                    >
                                        <Grid container>
                                            <Grid item sm={9} xs={12}>
                                                <TextField
                                                    fullWidth
                                                    value={searchTerm}
                                                    onChange={(e) =>
                                                        setSearchTerm(
                                                            e.target.value
                                                        )
                                                    }
                                                />
                                            </Grid>
                                            <Grid
                                                container
                                                item
                                                sm={3}
                                                spacing={1}
                                                xs={12}
                                            >
                                                <Grid item>
                                                    <Button type="submit">
                                                        {t("label.search")}
                                                    </Button>
                                                </Grid>

                                                {filteredDetectionEvaluations.length !==
                                                    detectionEvaluations.length && (
                                                    <Grid item>
                                                        <Button
                                                            onClick={() => {
                                                                setSearchTerm(
                                                                    ""
                                                                );
                                                                onSearchTermChange(
                                                                    ""
                                                                );
                                                                handleSearchClick(
                                                                    ""
                                                                );
                                                            }}
                                                        >
                                                            {t("label.reset")}
                                                        </Button>
                                                    </Grid>
                                                )}
                                            </Grid>
                                        </Grid>
                                    </form>
                                </Grid>
                            </Grid>

                            <Grid item xs={12}>
                                <Box paddingTop={1}>
                                    <Divider />
                                </Box>
                            </Grid>
                        </Grid>
                    </CardContent>
                )}

                <CardContent>
                    <Grid container justifyContent="space-between">
                        <Grid item>
                            <Pluralize
                                count={filteredDetectionEvaluations.length}
                                plural="items"
                                singular="item"
                            />
                        </Grid>
                        <Grid item>
                            {filteredDetectionEvaluations.length > 1 && (
                                <Button
                                    color="primary"
                                    variant="text"
                                    onClick={() =>
                                        onSortOrderChange(
                                            sortOrder ===
                                                DataGridSortOrderV1.DESC
                                                ? DataGridSortOrderV1.ASC
                                                : DataGridSortOrderV1.DESC
                                        )
                                    }
                                >
                                    {sortOrder === DataGridSortOrderV1.DESC && (
                                        <Grid container alignItems="center">
                                            <Grid item>
                                                <ArrowDownwardIcon />
                                            </Grid>
                                            <Grid item>
                                                {t("label.latest-one-first")}
                                            </Grid>
                                        </Grid>
                                    )}
                                    {sortOrder === DataGridSortOrderV1.ASC && (
                                        <Grid container alignItems="center">
                                            <Grid item>
                                                <ArrowUpwardIcon />
                                            </Grid>
                                            <Grid item>
                                                {t("label.latest-one-last")}
                                            </Grid>
                                        </Grid>
                                    )}
                                </Button>
                            )}
                        </Grid>
                    </Grid>
                    <Grid container>
                        {sortedDetectionEvaluations.map(
                            (detectionEvaluation) => {
                                return (
                                    <EnumerationItemRow
                                        alertId={alertId}
                                        anomalies={
                                            detectionEvaluation.anomalies
                                        }
                                        detectionEvaluation={
                                            detectionEvaluation
                                        }
                                        expanded={expanded}
                                        key={generateNameForDetectionResult(
                                            detectionEvaluation
                                        )}
                                        onExpandChange={handleIsOpenChange}
                                    />
                                );
                            }
                        )}
                    </Grid>
                </CardContent>
            </Card>
        );
    };
