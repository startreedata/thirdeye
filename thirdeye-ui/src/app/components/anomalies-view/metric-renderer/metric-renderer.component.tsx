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
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../../platform/components";
import { useGetAlert } from "../../../rest/alerts/alerts.actions";
import { getMetricString } from "../../../utils/anomalies/anomalies.util";
import { MetricRendererProps } from "./metric-renderer.interfaces";

export const MetricRenderer: FunctionComponent<MetricRendererProps> = ({
    anomaly,
    alertData,
}) => {
    const { t } = useTranslation();
    const { alert, getAlert } = useGetAlert();

    useEffect(() => {
        if (anomaly && !alertData) {
            getAlert(anomaly.alert.id);
        }
    }, [anomaly]);

    const alertDataToUse = useMemo(() => {
        if (alertData) {
            return alertData;
        }

        return alert;
    }, [alert, alertData]);

    return (
        <Grid container data-testId="anomaly-metric" spacing={1}>
            <Grid item color="text.disabled">
                <Typography color="textSecondary" variant="body1">
                    {t("label.metric")}:
                </Typography>
            </Grid>
            <Grid item>
                <Typography color="textSecondary" variant="body1">
                    {anomaly?.metadata.dataset?.name}:
                </Typography>
            </Grid>
            <Grid item>
                <Typography color="textPrimary" variant="body1">
                    {alertDataToUse && getMetricString(alertDataToUse, anomaly)}
                    {!alertDataToUse && <SkeletonV1 />}
                </Typography>
            </Grid>
        </Grid>
    );
};
