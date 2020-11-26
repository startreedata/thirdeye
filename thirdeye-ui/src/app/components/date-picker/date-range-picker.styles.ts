import { createStyles, makeStyles, Theme } from "@material-ui/core";

export const useDatePickerStyles = makeStyles((theme: Theme) =>
    createStyles({
        datePicker: {
            margin: theme.spacing(1),
        },
        buttonIcon: {
            padding: "7px 8px",
            minWidth: 0,
        },
    })
);
