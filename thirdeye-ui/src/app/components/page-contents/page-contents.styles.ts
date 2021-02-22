import { makeStyles, Theme } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const usePageContentsStyles = makeStyles((theme: Theme) => ({
    pageContents: {
        // Container to occupy entire height and width so that contained components can be
        // horizontally/vertically centered if required
        height: "100%",
        width: "100%",
        display: "flex",
        overflow: "auto", // This container can scroll while everything outside is fixed
    },
    main: {
        display: "flex",
        flexDirection: "column",
        paddingLeft: 16,
        paddingRight: 16,
    },
    mainExpanded: {
        flexGrow: 1, // Container to occupy available area
    },
    mainCenterAligned: {
        width: Dimension.WIDTH_PAGE_CONTENTS_DEFAULT,
        marginLeft: "auto",
        marginRight: "auto",
    },
    headerContainer: {
        position: "fixed",
        display: "flex",
        background: `linear-gradient(${theme.palette.background.default}, transparent)`,
        zIndex: theme.zIndex.drawer, // Header to be at the same level as drawer
    },
    header: {
        display: "flex",
        width: "100%",
        padding: 16,
        marginTop: 8,
    },
    headerContents: {
        height: 64,
    },
    headerPlaceholder: {
        minHeight: 110,
    },
    mainContents: {
        display: "flex",
        flexDirection: "column",
        flexGrow: 1, // Container to occupy available area
        paddingBottom: 16,
    },
}));
