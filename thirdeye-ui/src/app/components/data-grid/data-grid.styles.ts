import { makeStyles } from "@material-ui/core";

export const dataGridStyles = makeStyles({
    root: {
        "& .MuiDataGrid-cell:focus": {
            outline: "none",
        },
        "& .MuiDataGrid-colCell:focus": {
            outline: "none",
        },
    },
});
