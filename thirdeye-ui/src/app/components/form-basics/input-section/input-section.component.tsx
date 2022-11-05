import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { InputSectionProps } from "./input-section.interfaces";

export const InputSection: FunctionComponent<InputSectionProps> = ({
    label,
    helperLabel,
    labelComponent,
    inputComponent,
    fullWidth,
}) => {
    return (
        <Grid container item alignItems="center" xs={12}>
            <Grid item lg={2} md={4} sm={12} xs={12}>
                {!!labelComponent && labelComponent}
                {!labelComponent && label && (
                    <>
                        <Typography variant="body2">{label}</Typography>
                        {helperLabel && (
                            <Typography variant="caption">
                                {helperLabel}
                            </Typography>
                        )}
                    </>
                )}
            </Grid>
            <Grid
                item
                lg={fullWidth ? 10 : 4}
                md={fullWidth ? 8 : 5}
                sm={12}
                xs={12}
            >
                {inputComponent}
            </Grid>
        </Grid>
    );
};
