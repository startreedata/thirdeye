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
    pageContainerContents: {
        display: "flex",
        flex: 1,
        overflow: "hidden", // Contents can scroll while everything outside is fixed
    },
});
