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
import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import Image2 from "../../../assets/images/alert-type-examples/easy_alert_statistical.png";
import Image from "../../../assets/images/alert-type-examples/easy_alert_threshold.png";
import { Modal } from "../modal/modal.component";
import { EasyAlertModalProps } from "./easy-alert-modal.interfaces";

export const EasyAlertModal: FunctionComponent<EasyAlertModalProps> = ({
    onGotItClick,
    onCancel,
}) => {
    const { t } = useTranslation();

    return (
        <Modal
            initiallyOpen
            submitButtonLabel={t("label.got-it")}
            title={t("label.how-alert-algorithms-work")}
            onCancel={() => onCancel()}
            onSubmit={() => onGotItClick()}
        >
            <Grid container>
                <Grid item sm={4}>
                    <Typography paragraph variant="h6">
                        {t("label.threshold-based-alerting")}
                    </Typography>
                    <Typography variant="subtitle2">
                        {t("message.parametric_alerting_triggers_an_anomaly")}
                    </Typography>
                </Grid>
                <Grid item sm={8}>
                    <img alt={t("label.configure-notifications")} src={Image} />
                </Grid>
                <Grid item sm={4}>
                    <Typography paragraph variant="h6">
                        {t("label.statistical-anomaly-detection")}
                    </Typography>
                    <Typography variant="subtitle2">
                        {t("message.statistical-anomaly-detection-description")}
                    </Typography>
                </Grid>
                <Grid item sm={8}>
                    <img
                        alt={t("label.configure-notifications")}
                        src={Image2}
                    />
                </Grid>
            </Grid>
        </Modal>
    );
};
