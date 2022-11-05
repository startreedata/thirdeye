import { makeStyles } from "@material-ui/core";

export const usePageNotFoundIndicatorV1Styles = makeStyles((theme) => ({
    pageNotFoundIndicator: {
        height: "100%",
        width: "100%",
        display: "flex",
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    pageNotFoundIndicatorContents: {
        width: 800,
    },
    pageNotFoundIndicatorHeader: {
        marginBottom: theme.spacing(6),
    },
}));
