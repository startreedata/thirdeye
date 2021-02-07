import { makeStyles, Theme } from "@material-ui/core";
import { VisualizationCardProps } from "./visualization-card.interfaces";

const PADDING_OUTER_CONTAINER_MAXIMIZE = 32;
const PADDING_TOP_OUTER_CONTAINER_MAXIMIZE = 100;
const HEIGHT_HEADER_CONTAINER = 72;

export const useVisualizationCardStyles = makeStyles<
    Theme,
    VisualizationCardProps
>((theme: Theme) => ({
    outerContainer: (props) => ({
        height: `${HEIGHT_HEADER_CONTAINER + props.visualizationHeight}px`,
    }),
    outerContainerMaximize: (props) => ({
        position: "fixed",
        top: "50%",
        left: "50%",
        height: `${HEIGHT_HEADER_CONTAINER + props.visualizationHeight}px`, // Required height
        maxHeight: `calc(100% - ${
            PADDING_OUTER_CONTAINER_MAXIMIZE +
            PADDING_TOP_OUTER_CONTAINER_MAXIMIZE
        }px)`, // Available maxmized height
        width: `calc(100% - ${PADDING_OUTER_CONTAINER_MAXIMIZE * 2}px)`,
        transform: "translate(-50%, -50%)", // Center the container
        overflowY: "auto",
        zIndex: theme.zIndex.drawer + 3, // Maximized container to be always above backdrop
    }),
    headerContainer: {
        height: HEIGHT_HEADER_CONTAINER,
    },
    innerContainer: (props) => ({
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
