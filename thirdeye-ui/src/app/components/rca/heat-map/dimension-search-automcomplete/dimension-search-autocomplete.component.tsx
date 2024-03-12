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
    Box,
    Chip,
    IconButton,
    InputAdornment,
    TextField,
    Tooltip,
} from "@material-ui/core";
import CheckIcon from "@material-ui/icons/CheckCircleOutlineOutlined";
import CopyIcon from "@material-ui/icons/FileCopyOutlined";
import SearchIcon from "@material-ui/icons/Search";
import { Autocomplete } from "@material-ui/lab";
import { isString, pull } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { EMPTY_STRING_DISPLAY } from "../../../../utils/anomalies/anomalies.util";
import { copyToClipboard } from "../../../../utils/browser/browser.utils";
import { AnomalyFilterOption } from "../heat-map.interfaces";
import { formatDimensionOptions } from "../heat-map.utils";
import { DimensionSearchAutocompleteProps } from "./dimension-search-autocomplete.interfaces";
import { LocalThemeProviderV1 } from "../../../../platform/components";
import { lightV1 } from "../../../../platform/utils";

export const DimensionSearchAutocomplete: FunctionComponent<DimensionSearchAutocompleteProps> =
    ({ heatMapData, anomalyFilters, onFilterChange }) => {
        const { t } = useTranslation();

        // Keep track of the filter that was copied to the clipboard,
        // to show and hide a checkmark in response
        const [showCopyForFilter, setShowCopyForFilter] = useState<
            string | null
        >(null);

        const [anomalyFilterOptions, setAnomalyFilterOptions] = useState<
            AnomalyFilterOption[]
        >([]);

        useEffect(() => {
            if (!heatMapData) {
                return;
            }

            if (anomalyFilterOptions.length === 0) {
                setAnomalyFilterOptions(formatDimensionOptions(heatMapData));
            }
        }, [heatMapData]);

        const handleNodeFilterOnDelete = (node: AnomalyFilterOption): void => {
            const resultantFilters = pull(anomalyFilters, node);
            onFilterChange([...resultantFilters]);
        };

        const handleOnChangeFilter = (options: AnomalyFilterOption[]): void => {
            onFilterChange([...options]);
        };

        const handleCopyFilterName = (filterName: string): void => {
            copyToClipboard(filterName);
            setShowCopyForFilter(filterName);

            setTimeout(() => {
                setShowCopyForFilter(null);
            }, 2000);
        };

        const aggregateFilter = useMemo(
            () =>
                anomalyFilters
                    .map(
                        (option) =>
                            `${option.key}=${
                                option.value || EMPTY_STRING_DISPLAY
                            }`
                    )
                    .join("&"),
            [anomalyFilters]
        );

        return (
            <Autocomplete
                freeSolo
                multiple
                getOptionLabel={(option: AnomalyFilterOption) =>
                    isString(option.value)
                        ? option.value || EMPTY_STRING_DISPLAY
                        : ""
                }
                groupBy={(option: AnomalyFilterOption) =>
                    isString(option.key) ? option.key : ""
                }
                options={anomalyFilterOptions || []}
                renderInput={(params) => (
                    <TextField
                        {...params}
                        fullWidth
                        InputProps={{
                            ...params.InputProps,
                            /**
                             * Ensure that the chips are also
                             * rendered which are adornments
                             */
                            startAdornment: (
                                <>
                                    <InputAdornment position="start">
                                        <SearchIcon />
                                    </InputAdornment>
                                    {params.InputProps.startAdornment}
                                </>
                            ),
                            endAdornment: (
                                <>
                                    <InputAdornment position="start">
                                        <Tooltip
                                            title={
                                                aggregateFilter ===
                                                showCopyForFilter
                                                    ? t(
                                                          "label.copied-to-clipboard"
                                                      )
                                                    : t(
                                                          "label.copy-dimension-filter"
                                                      )
                                            }
                                        >
                                            <IconButton
                                                color="secondary"
                                                size="small"
                                                onClick={() => {
                                                    handleCopyFilterName(
                                                        aggregateFilter
                                                    );
                                                }}
                                            >
                                                {aggregateFilter ===
                                                showCopyForFilter ? (
                                                    <LocalThemeProviderV1
                                                        primary={
                                                            lightV1.palette
                                                                .success
                                                        }
                                                    >
                                                        <CheckIcon
                                                            color="primary"
                                                            fontSize="small"
                                                        />
                                                    </LocalThemeProviderV1>
                                                ) : (
                                                    <CopyIcon fontSize="small" />
                                                )}
                                            </IconButton>
                                        </Tooltip>
                                    </InputAdornment>
                                    {params.InputProps.endAdornment}
                                </>
                            ),
                        }}
                        placeholder={t("message.anomaly-filter-search")}
                        variant="outlined"
                    />
                )}
                renderTags={() =>
                    anomalyFilters
                        .map(
                            (option) =>
                                [
                                    option,
                                    `${option.key}=${
                                        option.value || EMPTY_STRING_DISPLAY
                                    }`,
                                ] as const
                        )
                        .map(([option, filterName], index) => (
                            <Chip
                                className="filter-chip"
                                key={`${index}_${option.value}`}
                                label={
                                    <Box
                                        alignItems="center"
                                        display="flex"
                                        gridGap={8}
                                    >
                                        {filterName}
                                        <Tooltip
                                            title={
                                                filterName === showCopyForFilter
                                                    ? t(
                                                          "label.copied-to-clipboard"
                                                      )
                                                    : t(
                                                          "label.copy-dimension-filter"
                                                      )
                                            }
                                        >
                                            <IconButton
                                                color="secondary"
                                                size="small"
                                                onClick={() => {
                                                    handleCopyFilterName(
                                                        filterName
                                                    );
                                                }}
                                            >
                                                {filterName ===
                                                showCopyForFilter ? (
                                                    <LocalThemeProviderV1
                                                        primary={
                                                            lightV1.palette
                                                                .success
                                                        }
                                                    >
                                                        <CheckIcon
                                                            color="primary"
                                                            fontSize="inherit"
                                                        />
                                                    </LocalThemeProviderV1>
                                                ) : (
                                                    <CopyIcon fontSize="inherit" />
                                                )}
                                            </IconButton>
                                        </Tooltip>
                                    </Box>
                                }
                                size="small"
                                style={{
                                    marginTop: 0,
                                    marginBottom: 0,
                                    marginRight: 3,
                                }}
                                onDelete={() =>
                                    handleNodeFilterOnDelete(option)
                                }
                            />
                        ))
                }
                value={anomalyFilters}
                onChange={(_e, options) =>
                    handleOnChangeFilter(options as AnomalyFilterOption[])
                }
            />
        );
    };
