import { makeStyles, Theme } from "@material-ui/core";
import { VisualizationCardProps } from "./visualization-card.interfaces";

const outerContainerMaximizePadding = 32;
const outerContainerMaximizePaddingTop = 100;
const headerContainerHeight = 72;

export const useVisualizationCardStyles = makeStyles<
    Theme,
    VisualizationCardProps
>((theme: Theme) => ({
    outerContainer: (props) => ({
        height: `${headerContainerHeight + props.visualizationHeight}px`,
        zIndex: theme.zIndex.drawer + 3, // Container to be always above backdrop
    }),
    outerContainerMaximize: (props) => ({
        position: "fixed",
        top: "50%",
        left: "50%",
        height: `${headerContainerHeight + props.visualizationHeight}px`, // Required height
        maxHeight: `calc(100% - ${
            outerContainerMaximizePadding + outerContainerMaximizePaddingTop
        }px)`, // Available maxmized height
        width: `calc(100% - ${outerContainerMaximizePadding * 2}px)`,
        transform: "translate(-50%, -50%)", // Center the container
        overflowY: "auto",
        zIndex: theme.zIndex.drawer + 3, // Container to be always above backdrop
    }),
    headerContainer: {
        height: headerContainerHeight,
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
