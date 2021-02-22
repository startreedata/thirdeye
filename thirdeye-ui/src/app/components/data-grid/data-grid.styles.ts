import { makeStyles } from "@material-ui/core";
import { Border } from "../../utils/material-ui/border.util";

export const useDataGridStyles = makeStyles((theme) => ({
    dataGrid: {
        border: Border.BORDER_DEFAULT,
        "& .MuiDataGrid-cell:focus": {
            outline: "none",
        },
        "& .MuiDataGrid-colCell:focus": {
            outline: "none",
        },
    },
    rowSelectionStatus: {
        padding: theme.spacing(2),
    },
}));
