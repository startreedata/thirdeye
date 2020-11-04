import { Button as MuiButton, withStyles } from "@material-ui/core";

export const Button = withStyles({
    root: {
        border: "2px solid #54BAC9",
        boxSizing: "border-box",
        borderRadius: "8px",
    },
})(MuiButton);
