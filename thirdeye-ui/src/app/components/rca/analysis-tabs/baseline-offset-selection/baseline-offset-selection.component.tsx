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
import { Box, Button, ButtonGroup, Grid, TextField } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    OFFSET_REGEX_EXTRACT,
    OFFSET_TO_HUMAN_READABLE,
} from "../../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.styles";
import { BaselineOffsetSelectionProps } from "./baseline-offset-selection.interfaces";

export const BaselineOffsetSelection: FunctionComponent<BaselineOffsetSelectionProps> =
    ({
        baselineOffset,
        onBaselineOffsetChange,
        label,
        inStart = false,
        hasReloadButton = true,
    }) => {
        const { t } = useTranslation();
        const classes = useAnomalyBreakdownComparisonHeatmapStyles();

        const [offsetUnit, setOffsetUnit] = useState<string>(() => {
            const result = OFFSET_REGEX_EXTRACT.exec(baselineOffset);

            if (result) {
                return result[2];
            }

            return "W";
        });
        const [offsetValue, setOffsetValue] = useState<number>(() => {
            const result = OFFSET_REGEX_EXTRACT.exec(baselineOffset);

            if (result) {
                return Number(result[1]);
            }

            return 1;
        });
        const availableOptions = useMemo(() => {
            return Object.entries(OFFSET_TO_HUMAN_READABLE).map(
                ([unit, label]) => {
                    return {
                        key: unit,
                        label,
                    };
                }
            );
        }, []);
        const isSetButtonDisabled = useMemo(() => {
            return baselineOffset === `P${offsetValue}${offsetUnit}`;
        }, [baselineOffset, offsetValue, offsetUnit]);

        const handleSetClick = (): void => {
            if (!offsetUnit || offsetValue === 0) {
                return;
            }

            onBaselineOffsetChange(`P${offsetValue}${offsetUnit}`);
        };

        useEffect(() => {
            if (!hasReloadButton) {
                handleSetClick();
            }
        }, [offsetUnit, offsetValue]);

        return (
            <Grid
                container
                alignItems="center"
                justifyContent={inStart ? "flex-start" : "flex-end"}
                spacing={0}
            >
                <Grid item>
                    <Box
                        className={classes.baselineWeekOffsetLabelContainer}
                        textAlign="right"
                    >
                        <label>
                            <strong>
                                {label ?? t("label.baseline-offset")}:
                            </strong>
                        </label>
                    </Box>
                </Grid>
                <Grid item>
                    <ButtonGroup color="primary">
                        <TextField
                            required
                            inputProps={{ style: { maxWidth: "30px" } }}
                            size="small"
                            type="number"
                            value={offsetValue}
                            onChange={(e) =>
                                setOffsetValue(Number(e.target.value))
                            }
                        />
                        <Autocomplete<{ key: string; label: string }>
                            autoSelect
                            classes={{
                                inputRoot: classes.input,
                            }}
                            getOptionLabel={(option) => option.label}
                            options={availableOptions}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    inputProps={{
                                        ...params.inputProps,
                                        style: {
                                            minWidth: "30px",
                                        },
                                    }}
                                    variant="outlined"
                                />
                            )}
                            value={{
                                key: offsetUnit,
                                label: OFFSET_TO_HUMAN_READABLE[offsetUnit],
                            }}
                            onChange={(_, selected) =>
                                selected && setOffsetUnit(selected.key)
                            }
                        />
                        {hasReloadButton && (
                            <Button
                                color="primary"
                                disabled={isSetButtonDisabled}
                                size="small"
                                variant="outlined"
                                onClick={handleSetClick}
                            >
                                {t("label.reload")}
                            </Button>
                        )}
                    </ButtonGroup>
                </Grid>
            </Grid>
        );
    };
