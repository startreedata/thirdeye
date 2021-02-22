import { makeStyles } from "@material-ui/core";

export const useAppBarStyles = makeStyles((theme) => ({
    appBar: {
        backgroundColor: theme.palette.background.default,
        zIndex: theme.zIndex.drawer + 1, // App bar to be always above drawer
    },
    link: {
        display: "flex",
        alignSelf: "center",
        marginRight: theme.spacing(2),
        "&:last-of-type": {
            marginRight: 0,
        },
    },
    linkRightAligned: {
        marginLeft: "auto",
    },
}));
