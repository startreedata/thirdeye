import { makeStyles } from "@material-ui/core";

export const useTransferListStyles = makeStyles({
    listContainer: {
        padding: "0px",
        height: "250px",
        overflowX: "hidden",
        overflowY: "auto",
        "&:last-child": {
            padding: "0px",
        },
    },
    listItem: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
});
