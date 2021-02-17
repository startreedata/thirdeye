import { ButtonBase, Grid, Paper, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, useEffect, useState } from "react";
import { ButtonTileProps } from "./button-tile.interfaces";
import { useButtonTileStyles } from "./button-tile.styles";

export const ButtonTile: FunctionComponent<ButtonTileProps> = (
    props: ButtonTileProps
) => {
    const buttonTileClasses = useButtonTileStyles(props);
    const [iconProperties, setIconProperties] = useState<
        Record<string, unknown>
    >();

    useEffect(() => {
        initIconProperties();
    }, []);

    const initIconProperties = (): void => {
        // Icon properties are initialized here, SVG fill to be assigned only if icon color is
        // provided
        // This helps retain original SVG colors
        const iconProperties: Record<string, unknown> = {};
        iconProperties.fontSize = "large";
        iconProperties.height = 50;
        if (props.iconColor) {
            iconProperties.fill = props.iconColor;
        }
        setIconProperties(iconProperties);
    };

    return (
        <ButtonBase
            focusRipple
            disabled={props.disabled}
            onClick={props.onClick}
        >
            <Paper className={buttonTileClasses.button} elevation={2}>
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
                                    <props.icon {...iconProperties} />
                                </Grid>
                            </Grid>
                        </Grid>
                    )}

                    {/* Text */}
                    <Grid
                        item
                        className={
                            props.textColor && buttonTileClasses.textColor
                        }
                    >
                        <Typography variant="button">{props.text}</Typography>
                    </Grid>
                </Grid>
            </Paper>
        </ButtonBase>
    );
};
