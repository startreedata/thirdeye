import { makeStyles } from "@material-ui/core";

export const useAppBarStyles = makeStyles((theme) => ({
    appBar: {
        backgroundColor: theme.palette.background.paper,
        zIndex: theme.zIndex.drawer + 1, // App bar above drawer
    },
    logo: {
        display: "flex",
        marginRight: theme.spacing(2),
        [theme.breakpoints.down("xs")]: {
            marginLeft: "auto", // Center the logo
        },
    },
    link: {
        marginRight: theme.spacing(2),
        "&:last-of-type": {
            marginRight: 0,
        },
    },
    linkRightAligned: {
        marginLeft: "auto",
    },
}));
