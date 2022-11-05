import { makeStyles } from "@material-ui/core";
import { BorderV1 } from "../../../../utils/material-ui/border.util";
import { DimensionV1 } from "../../../../utils/material-ui/dimension.util";

export const usePageHeaderV1Styles = makeStyles((theme) => ({
    pageHeader: {
        position: "sticky",
        top: 0,
        minHeight: DimensionV1.PageHeaderHeight,
        maxHeight: DimensionV1.PageHeaderHeight,
        backgroundColor: theme.palette.background.paper,
        borderBottom: BorderV1.BorderDefault,
        zIndex: theme.zIndex.appBar,
    },
    pageHeaderHidden: {
        minHeight: 0,
        maxHeight: 0,
    },
    pageHeaderGutters: {
        paddingLeft: theme.spacing(DimensionV1.PageGridSpacing),
        paddingRight: theme.spacing(DimensionV1.PageGridSpacing),
    },
    pageNotifications: {
        position: "sticky",
        backgroundColor: theme.palette.background.default,
        paddingLeft: theme.spacing(DimensionV1.PageGridSpacing),
        paddingRight: theme.spacing(DimensionV1.PageGridSpacing),
        zIndex: theme.zIndex.appBar,
    },
    pageNotificationsWithoutHeader: {
        top: 0,
    },
    pageNotificationsWithHeader: {
        top: DimensionV1.PageHeaderHeight,
    },
    pageNotificationsMinimized: {
        minHeight: theme.spacing(DimensionV1.PageGridSpacing),
        paddingTop: theme.spacing(DimensionV1.PageGridSpacing / 2),
        paddingBottom: theme.spacing(DimensionV1.PageGridSpacing / 2),
        zIndex: -1,
        transition: theme.transitions.create(["min-height", "z-index"], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    pageNotificationsMaximized: {
        minHeight:
            DimensionV1.AlertHeight +
            theme.spacing(DimensionV1.PageGridSpacing), // Default height of an alert and top and bottom padding
        paddingTop: theme.spacing(DimensionV1.PageGridSpacing / 2),
        paddingBottom: theme.spacing(DimensionV1.PageGridSpacing / 2),
        zIndex: theme.zIndex.appBar,
        transition: theme.transitions.create(["min-height", "z-index"], {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
}));
