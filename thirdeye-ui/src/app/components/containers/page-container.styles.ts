import { makeStyles, Theme } from "@material-ui/core";

export const pageContainerStyles = makeStyles((theme: Theme) => {
    return {
        main: {
            padding: theme.spacing(3),
            flexGrow: 1,
        },
        centered: {
            margin: "auto",
            maxWidth: "920px",
        },
    };
});
