import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui-util/dimension-util";

export const usePageContentsStyles = makeStyles({
    container: {
        // Avoid the effect of Material-UI Grid negative margins
        // Padding equivalent to default Grid spacing in Material-UI theme
        // https://material-ui.com/components/grid/#limitations
        padding: "8px",
    },
    outerContainer: {
        display: "flex",
        flexDirection: "column",
    },
    outerContainerExpand: {
        flexGrow: 1, // Container to occupy available area
        paddingLeft: "16px",
        paddingRight: "16px",
    },
    outerContainerCenterAlign: {
        width: Dimension.WIDTH_PAGE_CONTENTS_DEFAULT,
        marginLeft: "auto",
        marginRight: "auto",
    },
    headerContainer: {
        display: "flex",
        height: "70px",
        paddingTop: "16px",
        paddingBottom: "0px",
    },
    innerContainer: {
        display: "flex",
        flexDirection: "column",
        flexGrow: 1, // Container to occupy remaining available area
        paddingTop: "16px",
        paddingBottom: "16px",
    },
});
