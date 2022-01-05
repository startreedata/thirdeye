import { makeStyles } from "@material-ui/core";

const TABLE_CELL_SPACING = 3;

export const useDimensionHeatmapTooltipStyles = makeStyles((theme) => ({
    tableCell: {
        padding: TABLE_CELL_SPACING,
    },
    tableCellData: {
        textAlign: "right",
        padding: TABLE_CELL_SPACING,
    },
    tableCellLabel: {
        textAlign: "left",
        padding: TABLE_CELL_SPACING,
    },
    spaceBottom: {
        marginBottom: theme.spacing(1),
    },
    smallText: {
        fontSize: "x-small",
    },
}));
