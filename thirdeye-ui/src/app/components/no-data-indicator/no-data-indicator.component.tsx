import { Box, Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as EmptyGlassIcon } from "../../../assets/images/empty-glass.svg";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { NoDataIndicatorProps } from "./no-data-indicator.interfaces";
import { useNoDataIndicatorStyles } from "./no-data-indicator.styles";

export const NoDataIndicator: FunctionComponent<NoDataIndicatorProps> = (
    props: NoDataIndicatorProps
) => {
    const noDataIndicatorClasses = useNoDataIndicatorStyles();
    const theme = useTheme();
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
                className={noDataIndicatorClasses.icon}
                paddingRight={2}
            >
                <EmptyGlassIcon fill={theme.palette.primary.main} height={36} />
            </Box>

            {/* No data available message */}
            <Box paddingLeft={2}>
                <Typography variant="body2">
                    {props.text || t("message.no-data")}
                </Typography>
            </Box>
        </Box>
    );
};
