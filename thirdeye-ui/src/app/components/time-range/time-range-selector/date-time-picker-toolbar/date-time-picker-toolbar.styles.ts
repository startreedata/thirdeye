import { makeStyles } from "@material-ui/core";

export const useDateTimePickerToolbarStyles = makeStyles({
    toolbar: {
        marginBottom: "-10px", // Minimize whitespace between toolbar and calendar
    },
    toolbarDense: {
        minHeight: "36px",
    },
    link: {
        display: "flex",
        alignSelf: "center",
        marginRight: "8px",
        "&:last-of-type": {
            marginRight: "0px",
        },
    },
    linkRightAligned: {
        marginLeft: "auto",
    },
    linkSelected: {
        fontWeight: "bold",
    },
});
