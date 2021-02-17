import { Box, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { usePageNotFoundIndicatorStyles } from "./page-not-found-indicator.styles";

export const PageNotFoundIndicator: FunctionComponent = () => {
    const pageNotFoundIndicatorClasses = usePageNotFoundIndicatorStyles();
    const { t } = useTranslation();

    return (
        <Grid
            container
            alignItems="center"
            className={pageNotFoundIndicatorClasses.pageNotFoundIndicator}
            justify="center"
        >
            {/* Error code */}
            <Grid item>
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
            </Grid>

            {/* Error message */}
            <Grid item>
                <Typography variant="body2">
                    {t("message.page-not-found")}
                </Typography>
            </Grid>
        </Grid>
    );
};
