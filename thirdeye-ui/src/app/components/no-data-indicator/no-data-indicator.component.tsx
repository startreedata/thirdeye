import { Box, Grid, Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as EmptyGlass } from "../../../assets/images/empty-glass.svg";
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
        <Grid
            container
            alignItems="center"
            className={noDataIndicatorClasses.container}
            justify="center"
        >
            {/* Image */}
            <Grid item>
                <Box
                    border={Dimension.WIDTH_BORDER_DEFAULT}
                    borderBottom={0}
                    borderColor={Palette.COLOR_BORDER_DEFAULT}
                    borderLeft={0}
                    borderTop={0}
                    paddingRight={2}
                >
                    <EmptyGlass fill={theme.palette.primary.main} width={36} />
                </Box>
            </Grid>

            {/* No data available message */}
            <Grid item>
                <Typography variant="body2">
                    {props.text ? props.text : t("message.no-data")}
                </Typography>
            </Grid>
        </Grid>
    );
};
