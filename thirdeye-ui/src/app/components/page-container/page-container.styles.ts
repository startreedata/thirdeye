import { makeStyles } from "@material-ui/core";

export const usePageContainerStyles = makeStyles({
    outerContainer: {
        // Makes the container occupy entire height and width so that contained components can be
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
        flexGrow: 1, // Makes the inner container occupy entire available area
        overflowX: "hidden", // Makes this container scrollable and everything outside is fixed
    },
});
