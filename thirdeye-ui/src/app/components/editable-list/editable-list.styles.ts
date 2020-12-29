import { makeStyles } from "@material-ui/core";

export const useEditableListStyles = makeStyles({
    listContainer: {
        padding: "0px",
        maxHeight: "250px",
        "&:last-child": {
            padding: "0px",
        },
        overflow: "auto",
    },
});
