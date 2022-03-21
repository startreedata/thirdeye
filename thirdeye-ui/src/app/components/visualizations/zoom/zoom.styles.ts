import { makeStyles } from "@material-ui/core";
import { Color } from "../../../utils/material-ui/color.util";

export const useZoomStyles = makeStyles(() => ({
    container: {
        position: "relative",
    },
    controlInfo: {
        color: Color.WHITE_1,
    },
}));
