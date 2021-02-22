import { makeStyles } from "@material-ui/core";

export const useCommonStyles = makeStyles({
    gridLimitation: {
        // Padding equivalent to default Grid spacing in Material-UI theme to avoid the effect of
        // Material-UI Grid negative margins (https://material-ui.com/components/grid/#limitations)
        padding: "8px",
    },
    ellipsis: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    backdrop: {
        backgroundColor: "rgba(0, 0, 0, 0.5)",
    },
});
