import { makeStyles } from "@material-ui/core";

export const usePageHeaderTabsV1Styles = makeStyles({
    pageHeaderTabs: {
        position: "absolute",
        bottom: 0,
        display: "grid",
        paddingRight: "inherit", // Absolutely positioned element to respect padding of the parent
    },
});
