import { makeStyles } from "@material-ui/core";
import { BorderV1 } from "../../../utils/material-ui/border.util";
import { PaletteV1 } from "../../../utils/material-ui/palette.util";

export const HEIGHT_DATA_GRID_TOOLBAR = 56;
export const HEIGHT_DATA_GRID_HEADER_ROW = 40;
export const HEIGHT_DATA_GRID_ROW = 56;
export const WIDTH_DATA_GRID_ROW_SELECTION_COLUMN = 56;

export const useDataGridV1Styles = makeStyles((theme) => ({
    dataGrid: {
        overflow: "hidden",
        "& .BaseTable__table-main .BaseTable__header": {
            backgroundColor: PaletteV1.DataGridHeaderBackgroundColor,
        },
        "& .BaseTable__header-row": {
            ...theme.typography.subtitle2,
        },
        "& .BaseTable__header-cell--sortable:hover": {
            backgroundColor: theme.palette.action.focus,
        },
        "& .BaseTable__header-cell-text": {
            whiteSpace: "nowrap",
        },
        "& .BaseTable__table-main .BaseTable__header-cell:first-child, .BaseTable__table-main .BaseTable__row-cell:first-child":
            {
                paddingLeft: theme.spacing(2),
            },
        "& .BaseTable__table-main .BaseTable__header-cell:last-child, .BaseTable__table-main .BaseTable__row-cell:last-child":
            {
                paddingRight: theme.spacing(2),
            },
        "& .BaseTable__table-frozen-left .BaseTable__header-cell:first-child, .BaseTable__table-frozen-left .BaseTable__row-cell:first-child":
            {
                paddingLeft: theme.spacing(2),
            },
        "& .BaseTable__table-frozen-right .BaseTable__header-cell:last-child, .BaseTable__table-frozen-right .BaseTable__row-cell:last-child":
            {
                paddingRight: theme.spacing(2),
            },
        "& .BaseTable__sort-indicator": {
            marginLeft: 4,
        },
        "& .BaseTable__row": {
            minHeight: HEIGHT_DATA_GRID_ROW,
        },
        "& .BaseTable__row:hover, .BaseTable__row--hovered": {
            backgroundColor: theme.palette.action.focus,
        },
        "& .BaseTable__header-row, .BaseTable__row": {
            borderBottom: BorderV1.BorderDefault,
        },
        "& .BaseTable__row-expanded": {
            borderBottom: BorderV1.BorderDefault,
        },
        "& .BaseTable__resizing-line": {
            backgroundColor: BorderV1.BorderDefault,
        },
    },
    dataGridToolbar: {
        minHeight: HEIGHT_DATA_GRID_TOOLBAR,
        borderBottom: BorderV1.BorderDefault,
    },
    dataGridToolbarGutters: {
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
    },
    dataGridToolbarComponent: {
        flex: 1,
        "&>*:not(:last-child)": {
            marginRight: theme.spacing(1),
        },
    },
    dataGridToolbarSearch: {
        width: 225,
        marginLeft: theme.spacing(1),
    },
    dataGridToolbarSearchAndSelectionText: {
        marginLeft: theme.spacing(1),
    },
    dataGridCell: {
        textOverflow: "ellipsis",
        whiteSpace: "nowrap",
        overflow: "hidden",
    },
    dataGridSelectionCheckbox: {
        marginLeft: -theme.spacing(1),
    },
    dataGridRowSelected: {
        backgroundColor: theme.palette.action.selected,
    },
    dataGridCellSkeleton: {
        flex: 1,
    },
    dataGridExpandPanel: {
        padding: theme.spacing(2),
        width: "100%",
    },
}));
