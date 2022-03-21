import { makeStyles, Theme } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const usePageContentsStyles = makeStyles((theme: Theme) => ({
    pageContents: {
        display: "flex",
        flex: 1,
        overflow: "auto", // Container can scroll while everything outside is fixed
    },
    pageContentsWithAppDrawer: {
        [theme.breakpoints.down("sm")]: {
            marginLeft: Dimension.WIDTH_DRAWER_MINIMIZED,
        },
        [theme.breakpoints.up("md")]: {
            marginLeft: Dimension.WIDTH_DRAWER_DEFAULT,
        },
    },
    main: {
        display: "flex",
        flexDirection: "column",
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
    },
    mainExpanded: {
        flex: 1,
    },
    mainCenterAligned: {
        width: Dimension.WIDTH_PAGE_CONTENTS_CENTERED,
        marginLeft: "auto",
        marginRight: "auto",
    },
    headerContainer: {
        position: "fixed",
        background: `linear-gradient(${theme.palette.background.default}, ${theme.palette.background.default}00)`,
        zIndex: theme.zIndex.drawer, // Header at the same level as drawer
    },
    header: {
        minHeight: 78,
        display: "flex",
        padding: theme.spacing(2),
        marginTop: theme.spacing(1),
    },
    titleContainer: {
        flex: 1,
    },
    headerPlaceholder: {
        minHeight: 110,
    },
    mainContents: {
        display: "flex",
        flex: 1,
        flexDirection: "column",
        paddingBottom: theme.spacing(2),
    },
}));
