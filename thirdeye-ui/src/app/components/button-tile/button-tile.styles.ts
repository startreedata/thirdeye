import { makeStyles, Theme } from "@material-ui/core";
import { ButtonTileProps } from "./button-tile.interfaces";

export const useButtonTileStyles = makeStyles<Theme, ButtonTileProps>({
    buttonTile: {
        height: 130,
        width: 220,
        display: "flex",
    },
    icon: {
        height: 80,
    },
    iconColor: {
        color: (props) => props.iconColor,
    },
    textColor: {
        color: (props) => props.textColor,
    },
});
