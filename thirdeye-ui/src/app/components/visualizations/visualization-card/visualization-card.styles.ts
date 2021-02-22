import { makeStyles, Theme } from "@material-ui/core";
import { VisualizationCardProps } from "./visualization-card.interfaces";

const PADDING_TOP_CARD_MAXIMIZED = 100;
const PADDING_CARD_MAXIMIZED = 32;
const HEIGHT_HEADER = 72;

export const useVisualizationCardStyles = makeStyles<
    Theme,
    VisualizationCardProps
>((theme) => ({
    card: (props) => ({
        height: HEIGHT_HEADER + props.visualizationHeight,
        willChange: "position, top, left, width, height, transform",
    }),
    cardMaximized: (props) => ({
        position: "fixed",
        top: "50%",
        left: "50%",
        height: HEIGHT_HEADER + props.visualizationHeight, // Required height
        maxHeight: `calc(100% - ${
            PADDING_TOP_CARD_MAXIMIZED + PADDING_CARD_MAXIMIZED
        }px)`, // Available maxmized height, available height - top and bottom padding
        width: `calc(100% - ${PADDING_CARD_MAXIMIZED * 2}px)`, // Available width - left and right padding
        overflowY: "auto", // Allow vertical scroll in case maximized height is not sufficient
        zIndex: theme.zIndex.drawer + 3, // Maximized container to be always above backdrop
        WebkitTransform: "translate(-50%, -50%)", // Center the container
        willChange: "position, top, left, width, height, transform",
    }),
    header: {
        height: HEIGHT_HEADER,
    },
    contents: (props) => ({
        height: props.visualizationHeight,
    }),
    backdrop: {
        position: "fixed",
        top: 0,
        left: 0,
        height: "100%",
        width: "100%",
        zIndex: theme.zIndex.drawer + 2, // Backdrop to be always above app bar
    },
}));
