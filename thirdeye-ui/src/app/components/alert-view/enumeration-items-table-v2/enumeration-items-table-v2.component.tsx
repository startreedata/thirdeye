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
import {
    Button,
    Card,
    CardContent,
    Grid,
    InputAdornment,
    TextField,
    Typography,
} from "@material-ui/core";
import { Search } from "@material-ui/icons";
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import { sortBy } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { DataGridSortOrderV1 } from "../../../platform/components";
import { filterEnumerationItems } from "../../../utils/enumeration-items/enumeration-items.util";
import { EnumerationItemRowV2 } from "./enumeration-item-row/enumeration-item-row-v2.component";
import {
    EnumerationItemsTableV2Props,
    EnumerationItemsWithAnomalies,
} from "./enumeration-items-table-v2.interfaces";
import { useEnumerationItemsTableV2Styles } from "./enumeration-items-table-v2.styles";

const sortOptions = [
    {
        label: "Name",
        key: "enumerationItem.name",
    },
    {
        label: "Latest Anomaly",
        key: "lastAnomalyTs",
    },
];

export const EnumerationItemsTableV2: FunctionComponent<EnumerationItemsTableV2Props> =
    ({
        anomalies,
        enumerationsItems,
        alertId,
        startTime,
        endTime,
        expanded,
        onExpandedChange,
        initialSearchTerm,
        onSearchTermChange,
        sortOrder,
        onSortOrderChange,
        onSortKeyChange,
        sortKey,
    }) => {
        const classes = useEnumerationItemsTableV2Styles();

        const enumerationItemsWithAnomalies = useMemo(() => {
            const enumerationIdToObjects: {
                [key: number]: EnumerationItemsWithAnomalies;
            } = {};

            enumerationsItems.forEach((enumerationItem) => {
                enumerationIdToObjects[enumerationItem.id] = {
                    enumerationItem,
                    anomalies: [],
                    lastAnomalyTs: Number.MIN_SAFE_INTEGER,
                };
            });

            anomalies.forEach((anomaly) => {
                if (
                    anomaly.enumerationItem &&
                    enumerationIdToObjects[anomaly.enumerationItem.id]
                ) {
                    enumerationIdToObjects[
                        anomaly.enumerationItem.id
                    ].anomalies.push(anomaly);
                }
            });

            return Object.values(enumerationIdToObjects).map((item) => {
                item.lastAnomalyTs = Math.max(
                    ...item.anomalies.map((a) => a.startTime)
                );

                return item;
            });
        }, [anomalies, enumerationsItems]);

        const [
            filteredEnumerationItemsWithAnomalies,
            setFilteredEnumerationItemsWithAnomalies,
        ] = useState(
            filterEnumerationItems(
                enumerationItemsWithAnomalies,
                initialSearchTerm
            )
        );

        const [
            sortedEnumerationItemsWithAnomalies,
            setSortedEnumerationItemsWithAnomalies,
        ] = useState(filteredEnumerationItemsWithAnomalies);

        const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
        const { t } = useTranslation();

        const handleSearchClick = (term: string): void => {
            onSearchTermChange(term);
            if (term === "") {
                setFilteredEnumerationItemsWithAnomalies(
                    enumerationItemsWithAnomalies
                );

                return;
            }

            setFilteredEnumerationItemsWithAnomalies(
                filterEnumerationItems(
                    enumerationItemsWithAnomalies,
                    searchTerm
                )
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
                filteredEnumerationItemsWithAnomalies,
                sortKey ?? "lastAnomalyTs"
            );

            if (sortOrder === DataGridSortOrderV1.DESC) {
                copied = copied.reverse();
            }

            setSortedEnumerationItemsWithAnomalies(copied);
        }, [sortOrder, filteredEnumerationItemsWithAnomalies, sortKey]);

        useEffect(() => {
            setFilteredEnumerationItemsWithAnomalies(
                filterEnumerationItems(
                    enumerationItemsWithAnomalies,
                    searchTerm
                )
            );
        }, [enumerationItemsWithAnomalies]);

        const handleSortOptionClick = (sortOption: {
            label: string;
            key: string;
        }): void => {
            if (sortOption.key === sortOption.key) {
                if (sortOrder === DataGridSortOrderV1.DESC) {
                    onSortOrderChange(DataGridSortOrderV1.ASC);
                } else {
                    onSortOrderChange(DataGridSortOrderV1.DESC);
                }
            }

            onSortKeyChange(sortOption.key);
        };

        return (
            <Card variant="outlined">
                <CardContent className={classes.cardContent}>
                    <Grid
                        container
                        alignItems="center"
                        className={classes.sortContainer}
                        justifyContent="space-between"
                    >
                        <Grid item>
                            {filteredEnumerationItemsWithAnomalies.length >
                                1 && (
                                <Grid container alignItems="center">
                                    <Grid item>
                                        <Typography
                                            className={classes.sortLabel}
                                            variant="body2"
                                        >
                                            {t("label.sort-by")}:
                                        </Typography>
                                    </Grid>
                                    <Grid item>
                                        <>
                                            {sortOptions.map((sortOption) => {
                                                return (
                                                    <Button
                                                        className={
                                                            sortKey ===
                                                            sortOption.key
                                                                ? classes.activeSortButton
                                                                : classes.sortButton
                                                        }
                                                        key={sortOption.label}
                                                        variant="text"
                                                        onClick={() =>
                                                            handleSortOptionClick(
                                                                sortOption
                                                            )
                                                        }
                                                    >
                                                        {sortOption.label}
                                                        {sortOrder ===
                                                            DataGridSortOrderV1.DESC &&
                                                            sortKey ===
                                                                sortOption.key && (
                                                                <ArrowDownwardIcon fontSize="small" />
                                                            )}
                                                        {sortOrder ===
                                                            DataGridSortOrderV1.ASC &&
                                                            sortKey ===
                                                                sortOption.key && (
                                                                <ArrowUpwardIcon fontSize="small" />
                                                            )}
                                                    </Button>
                                                );
                                            })}
                                        </>
                                    </Grid>
                                </Grid>
                            )}
                        </Grid>
                        <Grid item>
                            <form
                                onSubmit={(e) => {
                                    e.preventDefault();
                                    handleSearchClick(searchTerm);
                                }}
                            >
                                <Grid container>
                                    <Grid
                                        item
                                        sm={
                                            filteredEnumerationItemsWithAnomalies.length !==
                                            enumerationsItems.length
                                                ? 9
                                                : 12
                                        }
                                        xs={12}
                                    >
                                        <TextField
                                            fullWidth
                                            InputProps={{
                                                startAdornment: (
                                                    <InputAdornment position="start">
                                                        <Search />
                                                    </InputAdornment>
                                                ),
                                            }}
                                            placeholder={t(
                                                "label.search-entity",
                                                {
                                                    entity: t(
                                                        "label.dimensions"
                                                    ),
                                                }
                                            )}
                                            value={searchTerm}
                                            onChange={(e) =>
                                                setSearchTerm(e.target.value)
                                            }
                                        />
                                    </Grid>
                                    {filteredEnumerationItemsWithAnomalies.length !==
                                        enumerationsItems.length && (
                                        <Grid
                                            container
                                            item
                                            alignItems="center"
                                            sm={3}
                                            spacing={1}
                                            xs={12}
                                        >
                                            <Grid item>
                                                <Button
                                                    variant="outlined"
                                                    onClick={() => {
                                                        setSearchTerm("");
                                                        onSearchTermChange("");
                                                        handleSearchClick("");
                                                    }}
                                                >
                                                    {t("label.reset")}
                                                </Button>
                                            </Grid>
                                        </Grid>
                                    )}
                                </Grid>
                            </form>
                        </Grid>
                    </Grid>
                    <Grid container>
                        {sortedEnumerationItemsWithAnomalies.map(
                            (enumerationItemWithAnomalies) => {
                                return (
                                    <Grid
                                        item
                                        key={JSON.stringify(
                                            enumerationItemWithAnomalies
                                                .enumerationItem.params
                                        )}
                                        xs={12}
                                    >
                                        <EnumerationItemRowV2
                                            alertId={alertId}
                                            anomalies={
                                                enumerationItemWithAnomalies.anomalies
                                            }
                                            endTime={endTime}
                                            enumerationItem={
                                                enumerationItemWithAnomalies.enumerationItem
                                            }
                                            expanded={expanded}
                                            startTime={startTime}
                                            onExpandChange={handleIsOpenChange}
                                        />
                                    </Grid>
                                );
                            }
                        )}
                    </Grid>
                </CardContent>
            </Card>
        );
    };
