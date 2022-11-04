import { makeStyles } from "@material-ui/core";
import { PaletteV1 } from "../../platform/utils";

export const useMetricsReportListStyles = makeStyles((theme) => ({
    fullWidthCell: {
        width: "100%",
    },
    tableHeader: {
        backgroundColor: PaletteV1.DataGridHeaderBackgroundColor,
        "&> th": {
            ...theme.typography.subtitle2,
            height: "39px",
            paddingBottom: 0,
            paddingTop: 0,
        },
    },
    expanded: {
        "&> th": {
            borderBottom: "none",
        },
        "&> td": {
            borderBottom: "none",
        },
    },
}));
