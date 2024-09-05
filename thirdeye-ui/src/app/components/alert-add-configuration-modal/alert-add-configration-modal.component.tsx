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
import { Box, Button, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import ControlPointOutlinedIcon from "@material-ui/icons/ControlPointOutlined";
import { Modal } from "../modal/modal.component";
import { AlertAddConfigrationModalProps } from "./alert-add-configration-modal.interfaces";
import Image from "../../../assets/images/alert-type-examples/configuration.png";
import {
    getSubscriptionGroupsCreatePathWithAlertId,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { boxCardStyles } from "./alert-add-configration-modal.styles";

export const AlertAddConfigrationModal: FunctionComponent<AlertAddConfigrationModalProps> =
    ({ alertId }) => {
        const { t } = useTranslation();
        const classes = boxCardStyles();

        return (
            <Modal
                initiallyOpen
                showFooter={false}
                submitButtonLabel={t("label.got-it")}
                title={t("label.alert-completed")}
            >
                <Box className={classes.card}>
                    <Grid container>
                        <Grid item>
                            <Box sx={{ width: "266px" }}>
                                <Typography variant="h6">
                                    {t("label.congratulations-,")}
                                </Typography>
                                <Typography variant="h6">
                                    {t("label.your-alert-has-been-created")}
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
                            <Box display="flex" gridGap={2}>
                                <Button
                                    color="primary"
                                    href={getSubscriptionGroupsCreatePathWithAlertId(
                                        alertId
                                    )}
                                    startIcon={<ControlPointOutlinedIcon />}
                                    variant="contained"
                                >
                                    {t("label.create-notifications")}
                                </Button>
                                <Button
                                    color="primary"
                                    href={getSubscriptionGroupsPath()}
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
