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
import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useGetAlert } from "../../../rest/alerts/alerts.actions";
import { MetricRendererProps } from "./metric-renderer.interfaces";

export const MetricRenderer: FunctionComponent<MetricRendererProps> = ({
    anomaly,
}) => {
    const { t } = useTranslation();
    const { alert, getAlert } = useGetAlert();

    useEffect(() => {
        if (anomaly) {
            getAlert(anomaly.alert.id);
        }
    }, [anomaly]);

    return (
        <Grid container spacing={1}>
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
                    {alert?.templateProperties?.aggregationFunction
                        ? `${alert?.templateProperties?.aggregationFunction}(${anomaly?.metadata.metric?.name})`
                        : anomaly?.metadata.metric?.name}
                </Typography>
            </Grid>
        </Grid>
    );
};
