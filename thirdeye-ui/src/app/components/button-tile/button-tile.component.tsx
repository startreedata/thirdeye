import { ButtonBase, Grid, Paper, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { ButtonTileProps } from "./button-tile.interfaces";
import { useButtonTileStyles } from "./button-tile.styles";

export const ButtonTile: FunctionComponent<ButtonTileProps> = (
    props: ButtonTileProps
) => {
    const buttonTileClasses = useButtonTileStyles();

    return (
        <ButtonBase
            focusRipple
            disabled={props.disabled}
            onClick={props.onClick}
        >
            <Paper className={buttonTileClasses.container} elevation={2}>
                {/* Outer grid, vertically aligns icon and text */}
                <Grid
                    container
                    alignItems="center"
                    direction="column"
                    justify="center"
                >
                    {props.icon && (
                        // Outer grid, vertically center aligns icon
                        <Grid item className={buttonTileClasses.iconContainer}>
                            <Grid
                                container
                                alignItems="center"
                                className={buttonTileClasses.iconContainer}
                                direction="column"
                                justify="center"
                            >
                                {/* Icon */}
                                <Grid item>
                                    <props.icon
                                        fill={props.iconColor}
                                        fontSize="large"
                                        height={50}
                                    />
                                </Grid>
                            </Grid>
                        </Grid>
                    )}

                    {/* Text */}
                    <Grid item>
                        <Typography color="primary" variant="button">
                            {props.text}
                        </Typography>
                    </Grid>
                </Grid>
            </Paper>
        </ButtonBase>
    );
};
