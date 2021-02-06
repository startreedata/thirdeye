import { makeStyles, Theme } from "@material-ui/core";

export const useAppBarStyles = makeStyles((theme: Theme) => ({
    container: {
        backgroundColor: theme.palette.background.default,
        zIndex: theme.zIndex.drawer + 1, // App bar to be always above drawer
    },
    link: {
        display: "flex",
        marginRight: "16px",
        "&:last-of-type": {
            marginRight: "0px",
        },
    },
    rightAlign: {
        marginLeft: "auto",
        marginRight: "16px",
    },
}));
