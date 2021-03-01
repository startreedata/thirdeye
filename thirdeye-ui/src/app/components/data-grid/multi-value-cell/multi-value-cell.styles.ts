import { makeStyles } from "@material-ui/core";

export const useMultiValueCellStyles = makeStyles((theme) => ({
    multiValueCell: {
        width: "100%",
    },
    linkValues: {
        color: theme.palette.primary.main,
    },
}));
