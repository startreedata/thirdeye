import { makeStyles } from "@material-ui/core";

export const useAnomalyBreakdownComparisonHeatmapStyles = makeStyles(
    (theme) => ({
        filtersContainer: {
            marginBottom: theme.spacing(2),
            "& .filter-chip": {
                margin: theme.spacing(1),
                backgroundColor: theme.palette.primary.main,
                color: theme.palette.common.white,
            },
        },
    })
);
