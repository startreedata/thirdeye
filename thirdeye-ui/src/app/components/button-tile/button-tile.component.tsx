import { ButtonBase, Grid, Paper, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, useEffect, useState } from "react";
import { ButtonTileProps } from "./button-tile.interfaces";
import { useButtonTileStyles } from "./button-tile.styles";

export const ButtonTile: FunctionComponent<ButtonTileProps> = (
    props: ButtonTileProps
) => {
    const buttonTileClasses = useButtonTileStyles(props);
    const [iconProps, setIconProps] = useState<Record<string, unknown>>();

    useEffect(() => {
        initIconProps();
    }, []);

    const initIconProps = (): void => {
        // SVG fill to be assigned only if icon color is provided, to retain original SVG colors
        const properties: Record<string, unknown> = {};
        properties.fontSize = "large";
        properties.height = 50;
        if (props.iconColor) {
            properties.fill = props.iconColor;
        }
        setIconProps(properties);
    };

    return (
        <ButtonBase
            focusRipple
            disabled={props.disabled}
            onClick={props.onClick}
        >
            <Paper className={buttonTileClasses.buttonTile} elevation={2}>
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
                                className={classnames(buttonTileClasses.icon, {
                                    [buttonTileClasses.iconColor]: Boolean(
                                        props.iconColor
                                    ),
                                })}
                            >
                                <Grid item>
                                    <props.icon {...iconProps} />
                                </Grid>
                            </Grid>
                        </Grid>
                    )}

                    {/* Text */}
                    {props.text && (
                        <Grid
                            item
                            className={
                                props.textColor && buttonTileClasses.textColor
                            }
                        >
                            <Typography variant="button">
                                {props.text}
                            </Typography>
                        </Grid>
                    )}
                </Grid>
            </Paper>
        </ButtonBase>
    );
};
