import { makeStyles } from "@material-ui/core";

export const useAnomaliesRowStyles = makeStyles(() => ({
    noBorder: {
        "&> th": {
            borderBottom: "none",
        },
        "&> td": {
            borderBottom: "none",
        },
    },
}));
