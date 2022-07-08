/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { EventRendererProps } from "./event-renderer.interfaces";

export const EventRenderer: FunctionComponent<EventRendererProps> = (
    props: EventRendererProps
) => {
    const { t } = useTranslation();

    const startTime =
        props.event && props.event.startTime
            ? formatDateAndTimeV1(props.event.startTime)
            : t("label.no-data-marker");
    const endTime =
        props.event && props.event.endTime
            ? formatDateAndTimeV1(props.event.endTime)
            : t("label.no-data-marker");

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
                    {(props.event && props.event.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Type */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.type")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.event && props.event.type) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Start Time */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.start-time")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">{startTime}</Typography>
            </Grid>

            {/* End Time */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.end-time")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">{endTime}</Typography>
            </Grid>

            {props.event?.targetDimensionMap && (
                <>
                    {/* Metadata */}
                    <Grid item xs={12}>
                        <Typography variant="subtitle1">
                            {t("label.event-metadata")}
                        </Typography>
                    </Grid>

                    {isEmpty(props.event.targetDimensionMap) ? (
                        <Grid item xs={12}>
                            {t("label.no-data-marker")}
                        </Grid>
                    ) : (
                        Object.keys(props.event.targetDimensionMap).map(
                            (propertyName) => (
                                <>
                                    <Grid item sm={3}>
                                        <Typography variant="subtitle1">
                                            <strong>{propertyName}</strong>
                                        </Typography>
                                    </Grid>

                                    <Grid item sm={9}>
                                        <Typography variant="body2">
                                            {props.event?.targetDimensionMap &&
                                                props.event.targetDimensionMap[
                                                    propertyName
                                                ].join(",")}
                                        </Typography>
                                    </Grid>
                                </>
                            )
                        )
                    )}
                </>
            )}
        </Grid>
    );
};
