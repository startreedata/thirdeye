import { Box, Button, Grid, TextField } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    OFFSET_REGEX_EXTRACT,
    OFFSET_TO_HUMAN_READABLE,
} from "../../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.styles";
import { BaselineOffsetSelectionProps } from "./baseline-offset-selection.interfaces";

export const BaselineOffsetSelection: FunctionComponent<BaselineOffsetSelectionProps> =
    ({ baselineOffset, onBaselineOffsetChange }) => {
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

        return (
            <Grid
                container
                alignItems="center"
                justifyContent="flex-end"
                spacing={0}
            >
                <Grid item>
                    <Box
                        className={classes.baselineWeekOffsetLabelContainer}
                        textAlign="right"
                    >
                        <label>
                            <strong>{t("label.baseline-offset")}:</strong>
                        </label>
                    </Box>
                </Grid>
                <Grid item>
                    <Grid
                        container
                        alignItems="center"
                        direction="row"
                        justifyContent="flex-start"
                    >
                        <Grid item xs={3}>
                            <TextField
                                required
                                size="small"
                                type="number"
                                value={offsetValue}
                                onChange={(e) =>
                                    setOffsetValue(Number(e.target.value))
                                }
                            />
                        </Grid>
                        <Grid item xs={5}>
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
                                        InputProps={{
                                            ...params.InputProps,
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
                        </Grid>
                        <Grid item>
                            <Button
                                color="primary"
                                disabled={isSetButtonDisabled}
                                variant="outlined"
                                onClick={handleSetClick}
                            >
                                {t("label.reload")}
                            </Button>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        );
    };
