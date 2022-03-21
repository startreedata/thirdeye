import { makeStyles } from "@material-ui/core";

export const useAnomalyBreakdownComparisonHeatmapStyles = makeStyles(
    (theme) => ({
        filtersContainer: {
            "& .filter-chip": {
                margin: theme.spacing(1),
                background: theme.palette.secondary,
            },
        },
    })
);
