import {
    ButtonBase,
    Grid,
    Paper,
    Typography,
    useTheme,
} from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { ButtonTileProps } from "./button-tile.interfaces";
import { useButtonTileStyles } from "./button-tile.styles";

export const ButtonTile: FunctionComponent<ButtonTileProps> = (
    props: ButtonTileProps
) => {
    const buttonTileClasses = useButtonTileStyles(props);
    const commonClasses = useCommonStyles();
    const [iconProps, setIconProps] = useState<Record<string, unknown>>();
    const theme = useTheme();

    useEffect(() => {
        initIconProps();
    }, []);

    const initIconProps = (): void => {
        // A collection of properties that work on both, Material-UI and custom SVG
        const properties: Record<string, unknown> = {};

        // For Material-UI SVG
        properties.fontSize = "large";
        properties.htmlColor =
            (props.disabled && theme.palette.action.disabled) ||
            props.iconColor ||
            theme.palette.action.active;

        // For custom SVG
        properties.height = 50;
        // To retain original custom SVG colors, SVG fill to be assigned only if icon color provided
        // or button disabled
        if (props.iconColor) {
            properties.fill = props.iconColor;
        }
        if (props.disabled) {
            properties.fill = theme.palette.action.disabled;
        }

        setIconProps(properties);
    };

    return (
        <Paper className={buttonTileClasses.buttonTile} elevation={2}>
            <ButtonBase
                focusRipple
                className={classnames(
                    buttonTileClasses.buttonBase,
                    commonClasses.gridLimitation
                )}
                disabled={props.disabled}
                onClick={props.onClick}
            >
                {/* Vertically center align icon and text */}
                <Grid
                    container
                    alignItems="center"
                    direction="column"
                    justify="center"
                >
                    {/* Icon */}
                    {props.icon && (
                        <Grid item>
                            {/* Vertically center align icon */}
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
                        <Grid item className={buttonTileClasses.text}>
                            <Typography variant="subtitle1">
                                {props.text}
                            </Typography>
                        </Grid>
                    )}
                </Grid>
            </ButtonBase>
        </Paper>
    );
};
