import { createStyles, makeStyles } from "@material-ui/core";

export const pageContainerStyles = makeStyles(
    createStyles({
        main: {
            display: "flex",
            flexFlow: "column",
            height: "100%",
        },
        container: {
            flexGrow: 1,
            overflowX: "hidden", // This eliminates the horizontal scroll introduced by full width Grid
        },
    })
);
