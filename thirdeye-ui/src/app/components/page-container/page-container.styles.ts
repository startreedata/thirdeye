import { makeStyles } from "@material-ui/core";

export const usePageContainerStyles = makeStyles({
    outerContainer: {
        // Container to occupy entire height and width so that contained components can be
        // horizontally/vertically centered if required
        height: "100%",
        width: "100%",
        display: "flex",
        flexDirection: "column",
    },
    innerContainer: {
        display: "flex",
        flexGrow: 1, // Container to occupy remaining available area
        overflowX: "hidden", // Disable horizontal scroll
        overflowY: "auto", // Contents of this container to be scrollable while everything outside is fixed
    },
});
