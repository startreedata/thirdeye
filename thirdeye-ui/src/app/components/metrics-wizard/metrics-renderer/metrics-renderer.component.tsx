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
import { Grid, Typography, useTheme } from "@material-ui/core";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { MetricRendererProps } from "./metrics-renderer.interfaces";

export const MetricRenderer: FunctionComponent<MetricRendererProps> = (
    props: MetricRendererProps
) => {
    const theme = useTheme();
    const { t } = useTranslation();

    return (
        <Grid container item justifyContent="flex-end">
            {/* Name */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.name")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.metric && props.metric.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Active */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.active")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    <>
                        {/* Active */}
                        {props.metric.active && (
                            <CheckIcon
                                fontSize="small"
                                htmlColor={theme.palette.success.main}
                            />
                        )}

                        {/* Inactive */}
                        {!props.metric.active && (
                            <CloseIcon
                                fontSize="small"
                                htmlColor={theme.palette.error.main}
                            />
                        )}
                    </>
                </Typography>
            </Grid>

            {/* Datasets */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.dataset")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.metric && props.metric.dataset?.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Aggregation Function */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.aggregation-function")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.metric && props.metric.aggregationFunction) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Threshold */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.threshold")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.metric && props.metric.rollupThreshold) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>
        </Grid>
    );
};
