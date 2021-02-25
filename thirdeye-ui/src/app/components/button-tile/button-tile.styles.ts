import { makeStyles, Theme } from "@material-ui/core";
import { ButtonTileProps } from "./button-tile.interfaces";

export const useButtonTileStyles = makeStyles<Theme, ButtonTileProps>(
    (theme) => ({
        buttonTile: {
            height: 130,
            width: 220,
            display: "flex",
        },
        buttonBase: {
            display: "flex",
            flex: 1,
            borderRadius: theme.shape.borderRadius,
        },
        icon: {
            height: 80,
        },
        text: {
            color: (props) =>
                (props.disabled && theme.palette.text.disabled) ||
                props.textColor ||
                theme.palette.text.primary,
        },
    })
);
