import { makeStyles } from "@material-ui/core";

export const useTimeRangeListStyles = makeStyles({
    listLabel: {
        paddingBottom: "0px",
        marginBottom: "-4px", // Minimize whitespace space between label and next list item
    },
    listItem: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    selectedListItem: {
        fontWeight: "bold",
    },
});
