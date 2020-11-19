import { makeStyles } from "@material-ui/core";

export const usePageContainerStyles = makeStyles({
    outerContainer: {
        display: "flex",
        flexFlow: "column",
        height: "100%",
    },
    innerContainer: {
        flexGrow: 1,
        overflowX: "hidden", // This eliminates the horizontal scroll introduced by full width Grid
    },
});
