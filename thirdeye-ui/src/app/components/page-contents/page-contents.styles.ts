import { makeStyles, Theme } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const usePageContentsStyles = makeStyles((theme: Theme) => ({
    pageContents: {
        display: "flex",
        flex: 1, // Occupy available area
        overflow: "auto", // Container can scroll while everything outside is fixed
    },
    main: {
        display: "flex",
        flexDirection: "column",
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
    },
    mainExpanded: {
        flex: 1, // Occupy available area
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
    headerContents: {
        flex: 1,
    },
    titleContainer: {
        flex: 1,
    },
    headerPlaceholder: {
        minHeight: 110,
    },
    mainContents: {
        display: "flex",
        flexDirection: "column",
        flex: 1, // Occupy available area
        paddingBottom: theme.spacing(2),
    },
}));
