import { makeStyles } from "@material-ui/core";

export const useDateTimePickerToolbarStyles = makeStyles({
    container: {
        marginTop: "8px",
        marginBottom: "-10px", // Minimize whitespace between toolbar and calendar
    },
    dense: {
        minHeight: "36px",
    },
    link: {
        marginRight: "8px",
        "&:last-of-type": {
            marginRight: "0px",
        },
    },
    rightAlign: {
        marginLeft: "auto",
    },
    selectedLink: {
        fontWeight: "bold",
    },
});
