import { makeStyles } from "@material-ui/core";

export const HEIGHT_NAV_BAR_LINK = 64;
export const PADDING_NAV_BAR_LINK = 20;

export const useNavBarLinkV1Styles = makeStyles({
    navBarLink: {
        minHeight: HEIGHT_NAV_BAR_LINK,
        maxHeight: HEIGHT_NAV_BAR_LINK,
    },
    navBarLinkGutters: {
        paddingLeft: PADDING_NAV_BAR_LINK,
        paddingRight: PADDING_NAV_BAR_LINK,
    },
});
