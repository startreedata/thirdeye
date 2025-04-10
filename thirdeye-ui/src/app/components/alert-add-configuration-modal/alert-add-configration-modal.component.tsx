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
import { Box, Button, Grid, IconButton, Typography } from "@material-ui/core";
import { Cancel } from "@material-ui/icons";
import ControlPointOutlinedIcon from "@material-ui/icons/ControlPointOutlined";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import Image from "../../../assets/images/alert-type-examples/configuration.png";
import { QUERY_PARAM_KEY_ALERT_TYPE } from "../../pages/alerts-view-page/alerts-view-page.utils";
import {
    getSubscriptionGroupsCreatePathWithAlertId,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { Modal } from "../modal/modal.component";
import { AlertAddConfigrationModalProps } from "./alert-add-configration-modal.interfaces";
import { boxCardStyles } from "./alert-add-configration-modal.styles";

export const AlertAddConfigurationModal: FunctionComponent<AlertAddConfigrationModalProps> =
    ({ alertId, open, onClose }) => {
        const { t } = useTranslation();
        const classes = boxCardStyles();
        const [searchParams] = useSearchParams();

        return (
            <Modal
                customTitle={
                    <Box className={classes.dialogTitle}>
                        <Typography variant="h6">
                            {t("label.alert-completed")}
                        </Typography>
                        <IconButton onClick={onClose}>
                            <Cancel color="secondary" />
                        </IconButton>
                    </Box>
                }
                isOpen={open}
                showFooter={false}
                submitButtonLabel={t("label.got-it")}
                onClose={onClose}
            >
                <Box className={classes.card}>
                    <Grid container>
                        <Grid item>
                            <Box sx={{ width: "266px" }}>
                                <Typography variant="h6">
                                    {t("label.congratulations-,")}
                                </Typography>
                                <Typography variant="h6">
                                    {searchParams.get(
                                        QUERY_PARAM_KEY_ALERT_TYPE
                                    ) === "create"
                                        ? t("label.your-alert-has-been-created")
                                        : t(
                                              "label.your-alert-has-been-updated"
                                          )}
                                </Typography>
                                <Box sx={{ my: 2 }}>
                                    <Typography variant="subtitle2">
                                        {t(
                                            "message.to-receive-notifications-configure"
                                        )}
                                    </Typography>
                                </Box>
                            </Box>
                        </Grid>
                        <Grid item>
                            <Box>
                                <img
                                    alt={t("label.configure-notifications")}
                                    src={Image}
                                />
                            </Box>
                        </Grid>
                        <Grid item>
                            <Box display="flex" gridGap={10}>
                                <Button
                                    color="primary"
                                    href={getSubscriptionGroupsCreatePathWithAlertId(
                                        alertId
                                    )}
                                    size="small"
                                    startIcon={<ControlPointOutlinedIcon />}
                                    variant="contained"
                                >
                                    {t("label.create-notifications")}
                                </Button>
                                <Button
                                    color="primary"
                                    href={getSubscriptionGroupsPath()}
                                    size="small"
                                    variant="outlined"
                                >
                                    {t("label.explore-all-notifications")}
                                </Button>
                            </Box>
                        </Grid>
                    </Grid>
                </Box>
            </Modal>
        );
    };
