import { makeStyles } from "@material-ui/core";

export const useTreemapStyles = makeStyles(() => ({
    heading: {
        fontSize: "13px",
    },
    clickable: {
        cursor: "pointer",
    },
    headingOtherDimension: {
        fontSize: "13px",
        fill: "rgba(0, 0, 0, 0.45)",
    },
}));
