import {
    Backdrop,
    CircularProgress,
    createStyles,
    makeStyles,
    Theme,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        backdrop: {
            zIndex: theme.zIndex.drawer + 1,
            color: "#fff",
        },
    })
);

export const AppLoader: FunctionComponent<{ visible: boolean }> = ({
    visible,
}: {
    visible: boolean;
}) => {
    const classes = useStyles();

    return (
        <Backdrop className={classes.backdrop} open={visible}>
            <CircularProgress color="inherit" />
        </Backdrop>
    );
};
