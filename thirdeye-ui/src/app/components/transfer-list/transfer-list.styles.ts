import { makeStyles } from "@material-ui/core";

export const useTransferListStyles = makeStyles({
    listContainer: {
        padding: "0px",
        height: "250px",
        "&:last-child": {
            padding: "0px",
        },
        overflow: "auto",
    },
    listItem: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
});
