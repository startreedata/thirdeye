import { makeStyles } from "@material-ui/core";

export const useLegendStyles = makeStyles((theme) => ({
    legend: {
        display: "flex",
        justifyContent: "space-between",
        marginLeft: "10px",
        marginRight: "10px",
    },
    legendItem: {
        cursor: "pointer",
    },
    legendItemLabel: {
        marginLeft: "10px",
    },
    disabledlegendItemLabel: {
        color: theme.palette.text.disabled,
    },
}));
