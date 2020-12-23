import { createStyles, makeStyles } from "@material-ui/core";

export const useSubscriptinoGroupCardStyles = makeStyles(() => {
    return createStyles({
        bottomRowLabel: {
            float: "left",
        },
        bottomRowIcon: {
            marginBottom: "-6px",
        },
        bottomRowValue: {
            clear: "both",
        },
    });
});
