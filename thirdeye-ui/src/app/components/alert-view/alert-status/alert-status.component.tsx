/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Grid, Typography, useTheme } from "@material-ui/core";
import CheckCircleOutlineIcon from "@material-ui/icons/CheckCircleOutline";
import HighlightOffIcon from "@material-ui/icons/HighlightOff";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AlertStatusProps } from "./alert-status.interfaces";

export const AlertStatus: FunctionComponent<AlertStatusProps> = ({ alert }) => {
    const theme = useTheme();
    const { t } = useTranslation();

    return (
        <>
            {alert?.active && (
                <Grid container alignItems="center" direction="row">
                    <Grid item>
                        <CheckCircleOutlineIcon
                            htmlColor={theme.palette.success.main}
                        />
                    </Grid>
                    <Grid item>
                        <Typography variant="h6">
                            <Box color={theme.palette.success.main}>
                                {t("message.alert-is-active")}
                            </Box>
                        </Typography>
                    </Grid>
                </Grid>
            )}
            {!alert?.active && (
                <Grid container alignItems="center" direction="row">
                    <Grid item>
                        <HighlightOffIcon
                            htmlColor={theme.palette.error.main}
                        />
                    </Grid>
                    <Grid item>
                        <Typography variant="h6">
                            <Box color={theme.palette.error.main}>
                                {t("message.alert-is-inactive")}
                            </Box>
                        </Typography>
                    </Grid>
                </Grid>
            )}
        </>
    );
};
