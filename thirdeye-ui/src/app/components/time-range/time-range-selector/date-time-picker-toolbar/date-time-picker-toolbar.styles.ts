import { makeStyles } from "@material-ui/core";

export const useDateTimePickerToolbarStyles = makeStyles((theme) => ({
    dateTimePickerToolbar: {
        marginBottom: theme.spacing(-1), // Minimize whitespace between toolbar and calendar
    },
    toolbarDense: {
        minHeight: 36,
    },
    link: {
        marginRight: theme.spacing(1),
        "&:last-of-type": {
            marginRight: 0,
        },
    },
    linkRightAligned: {
        marginLeft: "auto",
    },
    linkSelected: {
        fontWeight: "bold",
    },
}));
