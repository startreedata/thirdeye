import { makeStyles } from "@material-ui/core";

export const useHelpLinkIconV1Styles = makeStyles((theme) => ({
    helpIconFlex: {
        display: "flex",
    },
    helpIconInline: {
        display: "inline-block",
    },
    helpIconPadding: {
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
    },
}));
