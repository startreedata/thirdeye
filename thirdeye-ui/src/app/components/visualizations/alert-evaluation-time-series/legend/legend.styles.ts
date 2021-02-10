import { makeStyles } from "@material-ui/core";

export const useLegendStyles = makeStyles((theme) => ({
    container: {
        display: "flex",
        justifyContent: "space-between",
        paddingLeft: "10px",
        paddingRight: "10px",
    },
    legendItem: {
        cursor: "pointer",
    },
    legendItemLabel: {
        paddingLeft: "10px",
    },
    legendItemLabelDisabled: {
        color: theme.palette.text.disabled,
    },
}));
