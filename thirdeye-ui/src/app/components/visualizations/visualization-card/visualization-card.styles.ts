import { makeStyles, Theme } from "@material-ui/core";
import { VisualizationCardProps } from "./visualization-card.interfaces";

const PADDING_TOP_CARD_MAXIMIZE = 100;
const PADDING_CARD_MAXIMIZE = 32;
const HEIGHT_HEADER = 72;

export const useVisualizationCardStyles = makeStyles<
    Theme,
    VisualizationCardProps
>((theme) => ({
    card: (props) => ({
        height: `${HEIGHT_HEADER + props.visualizationHeight}px`,
    }),
    cardMaximize: (props) => ({
        position: "fixed",
        top: "50%",
        left: "50%",
        height: `${HEIGHT_HEADER + props.visualizationHeight}px`, // Required height
        maxHeight: `calc(100% - ${
            PADDING_TOP_CARD_MAXIMIZE + PADDING_CARD_MAXIMIZE
        }px)`, // Available maxmized height, available height - top and bottom padding
        width: `calc(100% - ${PADDING_CARD_MAXIMIZE * 2}px)`, // Available width - left and right padding
        transform: "translate(-50%, -50%)", // Center the container
        overflowY: "auto", // Allow vertical scroll in case maximized height is not sufficient
        zIndex: theme.zIndex.drawer + 3, // Maximized container to be always above backdrop
    }),
    header: {
        height: `${HEIGHT_HEADER}px`,
    },
    contents: (props) => ({
        height: `${props.visualizationHeight}px`,
    }),
    backdrop: {
        position: "fixed",
        top: "0px",
        left: "0px",
        height: "100%",
        width: "100%",
        zIndex: theme.zIndex.drawer + 2, // Backdrop to be always above app bar
    },
}));
