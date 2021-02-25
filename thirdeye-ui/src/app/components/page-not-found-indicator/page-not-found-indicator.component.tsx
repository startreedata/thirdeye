import { Box, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";

export const PageNotFoundIndicator: FunctionComponent = () => {
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
            {/* Error code */}
            <Box
                border={Dimension.WIDTH_BORDER_DEFAULT}
                borderBottom={0}
                borderColor={Palette.COLOR_BORDER_DEFAULT}
                borderLeft={0}
                borderTop={0}
                paddingRight={2}
            >
                <Typography color="primary" variant="h3">
                    {t("label.404")}
                </Typography>
            </Box>

            {/* Error message */}
            <Box paddingLeft={2}>
                <Typography variant="body2">
                    {t("message.page-not-found")}
                </Typography>
            </Box>
        </Box>
    );
};
