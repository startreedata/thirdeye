/*
 * Copyright 2022 StarTree Inc
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
import { Box, Button, Grid, Typography, useTheme } from "@material-ui/core";
import CheckCircleOutlineIcon from "@material-ui/icons/CheckCircleOutline";
import HighlightOffIcon from "@material-ui/icons/HighlightOff";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    JSONEditorV1,
    useDialogProviderV1,
} from "../../../platform/components";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { AlertViewSubHeaderProps } from "./alert-sub-header.interfaces";

export const AlertViewSubHeader: FunctionComponent<AlertViewSubHeaderProps> = ({
    alert,
}) => {
    const { showDialog, hideDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const theme = useTheme();

    const handleViewConfigButtonClick = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            headerText: t("label.detection-configuration"),
            contents: (
                <JSONEditorV1<Alert> disableValidation readOnly value={alert} />
            ),
            width: "md",
            hideCancelButton: true,
            okButtonText: t("label.close"),
            onOk: hideDialog,
        });
    };

    return (
        <Grid container justifyContent="space-between">
            <Grid item>
                {alert.active && (
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
                {!alert.active && (
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
            </Grid>

            <Grid item>
                <Grid container alignItems="center" direction="row">
                    <Grid item>
                        <Button
                            color="primary"
                            variant="text"
                            onClick={handleViewConfigButtonClick}
                        >
                            {t("message.view-detection-configuration")}
                        </Button>
                    </Grid>
                    <Grid item>
                        <TimeRangeButtonWithContext btnGroupColor="primary" />
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};
