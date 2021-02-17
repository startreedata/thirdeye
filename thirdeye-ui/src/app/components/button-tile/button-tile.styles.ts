import { makeStyles, Theme } from "@material-ui/core";
import { ButtonTileProps } from "./button-tile.interfaces";

export const useButtonTileStyles = makeStyles<Theme, ButtonTileProps>({
    button: {
        height: "130px",
        width: "220px",
        display: "flex",
    },
    icon: {
        height: "80px",
    },
    iconColor: (props) => ({
        color: props.iconColor,
    }),
    textColor: (props) => ({
        color: props.textColor,
    }),
});
