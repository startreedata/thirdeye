import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui-util/dimension-util";

export const useEditableListStyles = makeStyles({
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
    addButton: {
        height: Dimension.HEIGHT_INPUT_DEFAULT,
    },
});
