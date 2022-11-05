import { makeStyles } from "@material-ui/core";

export const useTooltipStyles = makeStyles((theme) => ({
    table: {
        width: "100%",
    },
    valueCell: {
        textAlign: "right",
    },
    time: {
        marginBottom: theme.spacing(1),
    },
}));
