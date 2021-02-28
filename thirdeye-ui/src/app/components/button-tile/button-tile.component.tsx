import { Button, Grid, Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { ButtonTileProps } from "./button-tile.interfaces";
import { useButtonTileStyles } from "./button-tile.styles";

export const ButtonTile: FunctionComponent<ButtonTileProps> = (
    props: ButtonTileProps
) => {
    const buttonTileClasses = useButtonTileStyles();
    const [iconProps, setIconProps] = useState<Record<string, unknown>>();
    const theme = useTheme();

    useEffect(() => {
        // Icon color changed or buton enabled/disabled, initialize icon properties
        initIconProps();
    }, [props.iconColor, props.disabled]);

    const initIconProps = (): void => {
        // Properties that work on both, Material-UI and custom SVG
        const properties: Record<string, unknown> = {};

        // Material-UI SVG properties
        properties.fontSize = "large";
        properties.htmlColor = props.disabled
            ? theme.palette.action.disabled
            : props.iconColor || theme.palette.action.active;

        // Custom SVG properties
        properties.height = 50;
        // To retain original custom SVG colors, SVG fill to be assigned only if icon color provided
        // or button disabled
        if (props.iconColor) {
            properties.fill = props.iconColor;
        }
        if (props.disabled) {
            properties.fill = theme.palette.action.disabled;
            properties.opacity = 0.5;
        }

        setIconProps(properties);
    };

    return (
        // Using ButtonBase doesn't provide hover and active effects out of the box
        // Using Button instead helps avoid duplicating those effects
        <Button
            classes={{ root: buttonTileClasses.buttonRoot }}
            disabled={props.disabled}
            variant="contained"
            onClick={props.onClick}
        >
            <Grid
                container
                alignItems="center"
                direction="column"
                justify="center"
            >
                {/* Icon */}
                {props.icon && (
                    <Grid item>
                        <Grid
                            container
                            alignItems="center"
                            className={buttonTileClasses.icon}
                        >
                            <Grid item>
                                <props.icon {...iconProps} />
                            </Grid>
                        </Grid>
                    </Grid>
                )}

                {/* Text */}
                {props.text && (
                    <Grid item>
                        <Typography variant="subtitle1">
                            {props.text}
                        </Typography>
                    </Grid>
                )}
            </Grid>
        </Button>
    );
};
