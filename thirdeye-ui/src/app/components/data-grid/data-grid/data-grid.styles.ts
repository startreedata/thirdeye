import { makeStyles } from "@material-ui/core";
import { Border } from "../../../utils/material-ui/border.util";

export const useDataGridStyles = makeStyles({
    toolbar: {
        borderBottom: Border.BORDER_DEFAULT,
    },
    rowSelectionStatus: {
        marginLeft: "auto",
    },
    dataGrid: {
        border: Border.BORDER_DEFAULT,
        "& .MuiDataGrid-cell:focus": {
            outline: "none",
        },
        "& .MuiDataGrid-colCell:focus": {
            outline: "none",
        },
    },
});
