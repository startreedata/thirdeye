import { makeStyles } from "@material-ui/core";

export const usePageContainerStyles = makeStyles({
    pageContainer: {
        // Container to occupy entire height and width so that contained components can be
        // horizontally/vertically centered if required
        height: "100%",
        width: "100%",
        display: "flex",
        flexDirection: "column",
    },
    contents: {
        display: "flex",
        flexGrow: 1, // Container to occupy available area
        overflow: "hidden", // Contents of this container can scroll while everything outside is fixed
    },
});
