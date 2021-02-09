import { makeStyles } from "@material-ui/core";
import { theme } from "../../utils/material-ui/theme.util";

export const useMetricsListStyles = makeStyles({
    root: {
        "& .MuiDataGrid-cell:focus": {
            outline: "none",
        },
        "& .MuiDataGrid-colCell:focus": {
            outline: "none",
        },
    },
    toolbar: {
        flex: 1,
        padding: theme.spacing(1),
    },
    rightAlign: {
        marginRight: "16px",
        marginLeft: "auto",
    },
    searchContainer: {
        width: "50%",
    },
});
