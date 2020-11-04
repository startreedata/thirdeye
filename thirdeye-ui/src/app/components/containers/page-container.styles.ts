import { makeStyles, Theme } from "@material-ui/core";

export const pageContainerStyles = makeStyles((theme: Theme) => {
    return {
        main: {
            flexGrow: 1,
        },
        centered: {
            margin: "auto",
            maxWidth: "920px",
        },
        padding0: {
            padding: 0,
        },
        innerContainer: {
            padding: theme.spacing(3),
        },
    };
});
