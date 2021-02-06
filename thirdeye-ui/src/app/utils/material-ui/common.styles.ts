import { makeStyles } from "@material-ui/core";

export const useCommonStyles = makeStyles({
    gridLimitation: {
        // Avoid the effect of Material-UI Grid negative margins
        // Padding equivalent to default Grid spacing in Material-UI theme
        // https://material-ui.com/components/grid/#limitations
        padding: "8px",
    },
    backdrop: {
        backgroundColor: "rgba(0, 0, 0, 0.5)",
    },
});
