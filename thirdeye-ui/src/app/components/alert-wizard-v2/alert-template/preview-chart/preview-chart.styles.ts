import { makeStyles } from "@material-ui/core";

export const usePreviewChartStyles = makeStyles((theme) => ({
    heightWholeContainer: {
        height: "100%",
    },
    alertContainer: {
        paddingLeft: theme.spacing(5),
        paddingRight: theme.spacing(5),
        position: "absolute",
        textAlign: "center",
        width: "100%",
        height: "100%",
    },
    selected: {
        color: theme.palette.primary.main,
    },
}));
