import { makeStyles } from "@material-ui/core";

const PAGE_HEADER_OVERRIDE = {
    padding: "24px",
};

export const usePageHeaderStyles = makeStyles((theme) => {
    return {
        pageHeader: {
            ...PAGE_HEADER_OVERRIDE,
            backgroundColor: "#ffffff",
            borderBottom: "1px solid #DBDEE7",
            marginBottom: theme.spacing(2),
        },
        transparent: {
            ...PAGE_HEADER_OVERRIDE,
            backgroundColor: "transparent",
            "& .page-header-sub-nav": {
                borderBottom: "1px solid #DBDEE7",
                marginBottom: theme.spacing(2),
            },
        },
        noPaddingBottom: {
            paddingBottom: "0 !important",
        },
    };
});
