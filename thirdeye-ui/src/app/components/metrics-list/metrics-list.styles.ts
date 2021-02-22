import { makeStyles } from "@material-ui/core";
import { theme } from "../../utils/material-ui/theme.util";

export const useMetricsListStyles = makeStyles({
    metricsList: {
        flexGrow: 1,
    },
    list: {
        flexGrow: 1,
    },

    toolbar: {
        flex: 1,
        padding: theme.spacing(1),
    },
    rightAlign: {
        marginRight: 16,
        marginLeft: "auto",
    },
    listContainer: {
        display: "flex",
        flexWrap: "wrap",
        alignSelf: "center",
    },
    moreIcon: {
        marginLeft: "auto",
    },
    searchContainer: {
        width: "50%",
    },
    paddingRight: {
        paddingRight: theme.spacing(1),
    },
});
