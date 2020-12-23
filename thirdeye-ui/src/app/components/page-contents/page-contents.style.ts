import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui-util/dimension-util";

export const usePageContentsStyles = makeStyles({
    container: {
        // Avoids the effect of Material UI Grid negative margins
        // Padding equivalent to 2x Grid spacing in Material UI theme
        // https://material-ui.com/components/grid/#limitations
        padding: "16px",
    },
    containerExpand: {
        flexGrow: 1,
    },
    containerCenterAlign: {
        width: Dimension.WIDTH_PAGE_CONTENTS_DEFAULT,
        marginLeft: "auto",
        marginRight: "auto",
    },
    header: {
        minHeight: "80px",
    },
    titleCenterAlign: {
        flexGrow: 1,
        textAlign: "center",
    },
});
