import { makeStyles, Theme } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const usePageContentsStyles = makeStyles((theme: Theme) => ({
    outerContainer: {
        // Container to occupy entire height and width so that contained components can be
        // horizontally/vertically centered if required
        height: "100%",
        width: "100%",
        display: "flex",
        overflow: "auto", // This container can scroll while everything outside is fixed
    },
    innerContainer: {
        display: "flex",
        flexDirection: "column",
    },
    innerContainerExpand: {
        flexGrow: 1, // Container to occupy available area
        width: "100%",
        paddingLeft: "16px",
        paddingRight: "16px",
    },
    innerContainerCenterAlign: {
        width: Dimension.WIDTH_PAGE_CONTENTS_DEFAULT,
        minWidth: Dimension.WIDTH_PAGE_CONTENTS_DEFAULT,
        maxWidth: Dimension.WIDTH_PAGE_CONTENTS_DEFAULT,
        marginLeft: "auto",
        marginRight: "auto",
    },
    headerContainer: {
        display: "flex",
        position: "fixed",
        background: `linear-gradient(${theme.palette.background.default}, transparent)`,
        // Header to be at the same level as drawer
        zIndex: theme.zIndex.drawer,
    },
    headerContainerFullWidth: {
        width: "100%",
    },
    header: {
        display: "flex",
        width: "100%",
        height: "80px",
        padding: "16px",
        marginTop: "8px",
    },
    headerContents: {
        height: "64px",
    },
    headerPlaceholder: {
        height: "110px",
        minHeight: "110px",
        maxHeight: "110px",
    },
    contentsContainer: {
        display: "flex",
        flexDirection: "column",
        flexGrow: 1, // Container to occupy available area
    },
}));
