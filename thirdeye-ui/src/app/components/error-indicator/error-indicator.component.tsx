import { Box, Typography } from "@material-ui/core";
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
        <Box
            alignItems="center"
            display="flex"
            flex={1}
            height="100%"
            justifyContent="center"
            width="100%"
        >
            {/* Icon */}
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

            {/* Error message */}
            <Box paddingLeft={2}>
                <Typography variant="body2">
                    {props.text || t("message.error")}
                </Typography>
            </Box>
        </Box>
    );
};
