import { Button, Checkbox } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { LocalThemeProviderV1 } from "../../../../../platform/components";
import { lightV1 } from "../../../../../platform/utils";
import { concatKeyValueWithEqual } from "../../../../../utils/params/params.util";
import { FilteredTimeSeriesProps } from "./filtered-time-series-row.interfaces";
import { useFilteredTimeSeriesRowStyles } from "./filtered-time-series-row.styles";

export const FilteredTimeSeriesRow: FunctionComponent<
    FilteredTimeSeriesProps
> = ({ series, colorScale, filterSet, onRemoveBtnClick, onCheckBoxClick }) => {
    const classes = useFilteredTimeSeriesRowStyles();
    const { t } = useTranslation();

    const name = filterSet.map(concatKeyValueWithEqual).join(" & ");
    const seriesData = series.find((candidate) => candidate.name === name);

    let color = colorScale(name);

    if (seriesData && seriesData.color !== undefined) {
        color = seriesData.color;
    }

    if (!seriesData || !seriesData.enabled) {
        color = "#EEE";
    }

    return (
        <TableRow>
            <TableCell
                style={{
                    borderLeftWidth: "5px",
                    borderLeftColor: color,
                    borderLeftStyle: "solid",
                }}
            >
                {seriesData && (
                    <Checkbox
                        checked={seriesData.enabled}
                        size="small"
                        onChange={onCheckBoxClick}
                    />
                )}
            </TableCell>
            <TableCell>{t("label.metric")}</TableCell>
            <TableCell>{name}</TableCell>
            <TableCell align="right">
                <LocalThemeProviderV1 primary={lightV1.palette.error}>
                    <Button
                        className={classes.removeBtn}
                        color="primary"
                        size="small"
                        onClick={onRemoveBtnClick}
                    >
                        {t("label.remove-metric")}
                    </Button>
                </LocalThemeProviderV1>
            </TableCell>
        </TableRow>
    );
};
