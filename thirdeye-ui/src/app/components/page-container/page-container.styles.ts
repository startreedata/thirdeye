import { makeStyles } from "@material-ui/core";

export const usePageContainerStyles = makeStyles({
    outerContainer: {
        // Container to occupy entire height and width so that contained components can be
        // vertically centered if required
        height: "100%",
        width: "100%",
        display: "flex",
        flexFlow: "column",
        padding: "0px",
        margin: "0px",
    },
    innerContainer: {
        display: "flex",
        flexGrow: 1, // Make the inner container occupy entire available area
        overflowX: "hidden", // Make this container scrollable while everything outside is fixed
    },
});
