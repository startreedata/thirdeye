/*
 * Copyright 2023 StarTree Inc
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
import { useQuery } from "@tanstack/react-query";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { JSONEditorV1 } from "../../../platform/components";
import { getAlertInsight } from "../../../rest/alerts/alerts.rest";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { determineTimezoneFromAlertInEvaluation } from "../../../utils/alerts/alerts.util";
import { getAlertsUpdatePath } from "../../../utils/routes/routes.util";
import { Modal } from "../../modal/modal.component";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { AlertViewSubHeaderProps } from "./alert-sub-header.interfaces";

export const AlertViewSubHeader: FunctionComponent<AlertViewSubHeaderProps> = ({
    alert,
}) => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const theme = useTheme();

    const getAlertInsightQuery = useQuery({
        queryKey: ["alertInsight", alert.id],
        queryFn: () => {
            return getAlertInsight({ alertId: alert.id });
        },
        refetchOnWindowFocus: false,
    });

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
                        <Modal
                            cancelButtonLabel={t("label.close")}
                            footerActions={
                                <Button
                                    className="dialog-provider-v1-cancel-button"
                                    onClick={() => {
                                        navigate(getAlertsUpdatePath(alert.id));
                                    }}
                                >
                                    {t("label.edit")}
                                </Button>
                            }
                            maxWidth="md"
                            title={t("label.detection-configuration")}
                            trigger={(handleTriggerClick) => (
                                <Button
                                    color="primary"
                                    variant="text"
                                    onClick={handleTriggerClick}
                                >
                                    {t("message.view-detection-configuration")}
                                </Button>
                            )}
                        >
                            <JSONEditorV1<Alert>
                                disableValidation
                                readOnly
                                value={alert}
                            />
                        </Modal>
                    </Grid>
                    <Grid item>
                        <TimeRangeButtonWithContext
                            btnGroupColor="primary"
                            timezone={determineTimezoneFromAlertInEvaluation(
                                getAlertInsightQuery.data
                                    ?.templateWithProperties
                            )}
                        />
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};
