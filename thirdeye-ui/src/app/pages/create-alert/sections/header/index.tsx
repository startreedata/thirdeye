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
// external
import React from "react";
import { Box, Grid, Typography } from "@material-ui/core";
import { useTranslation } from "react-i18next";

// styles
import { easyAlertHeaderStyles } from "./styles";

export const CreateAlertHeader = (): JSX.Element => {
    const { t } = useTranslation();
    const componentStyles = easyAlertHeaderStyles();

    return (
        <Grid item xs={12}>
            <Box display="flex">
                <Typography className={componentStyles.header} variant="h5">
                    {/* {t("label.alert-wizard")} */}
                    Create Alert
                </Typography>
            </Box>
            <Box>
                <Typography variant="body2">
                    {t("message.lets-get-started")}
                </Typography>
            </Box>
        </Grid>
    );
};
