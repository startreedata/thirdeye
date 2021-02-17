import { Box, Grid, Typography } from "@material-ui/core";
import ErrorOutlineIcon from "@material-ui/icons/ErrorOutline";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { ErrorIndicatorProps } from "./error-indicator.interfaces";
import { useErrorIndicatorStyles } from "./error-indicator.styles";

export const ErrorIndicator: FunctionComponent<ErrorIndicatorProps> = (
    props: ErrorIndicatorProps
) => {
    const errorIndicatorClasses = useErrorIndicatorStyles();
    const { t } = useTranslation();

    return (
        <Grid
            container
            alignItems="center"
            className={errorIndicatorClasses.errorIndicator}
            justify="center"
        >
            {/* Icon */}
            <Grid item>
                <Box
                    border={Dimension.WIDTH_BORDER_DEFAULT}
                    borderBottom={0}
                    borderColor={Palette.COLOR_BORDER_DEFAULT}
                    borderLeft={0}
                    borderTop={0}
                    className={errorIndicatorClasses.icon}
                    paddingRight={2}
                >
                    <ErrorOutlineIcon color="error" fontSize="large" />
                </Box>
            </Grid>

            {/* Error message */}
            <Grid item>
                <Typography variant="body2">
                    {props.text || t("message.error")}
                </Typography>
            </Grid>
        </Grid>
    );
};
