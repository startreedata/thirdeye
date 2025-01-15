/*
 * Copyright 2025 StarTree Inc
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
import { forEach } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { UiAlert } from "../../../../../rest/dto/ui-alert.interfaces";
import { useAlertCardStyles } from "./styles";
import { NoDataIndicator } from "../../../../no-data-indicator/no-data-indicator.component";

type AlertCardV1Props = {
    uiAlert: UiAlert;
    showCreatedBy?: boolean;
};

export const AlertCardV1: FunctionComponent<AlertCardV1Props> = (
    props: AlertCardV1Props
) => {
    const classes = useAlertCardStyles();
    const { t } = useTranslation();

    const getAllSubscriptionGroupsName = (uiAlert: UiAlert): string => {
        const subScriptingGroups: Array<string | number> = [];
        forEach(uiAlert.subscriptionGroups, (Obj) => {
            if (Obj.name) {
                subScriptingGroups.push(Obj.name);
            }
        });

        return subScriptingGroups.join(", ");
    };

    const getDetectionTypes = (uiAlert: UiAlert): string => {
        return uiAlert.detectionTypes.join(", ");
    };

    if (!props.uiAlert) {
        return <NoDataIndicator />;
    }

    return (
        <Grid container>
            {/* Created By */}
            {props.showCreatedBy && (
                <Grid item xs={6}>
                    <Grid item>
                        <Typography
                            className={classes.fontMedium}
                            variant="body2"
                        >
                            {t("label.created-by")}:
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Typography variant="body2">
                            {props.uiAlert.createdBy || "-"}
                        </Typography>
                    </Grid>
                </Grid>
            )}

            {/* Detection Type */}
            {props.uiAlert.detectionTypes.length > 0 && (
                <Grid item xs={6}>
                    <Grid item>
                        <Typography
                            className={classes.fontMedium}
                            variant="body2"
                        >
                            {t("label.detection-type")}:
                        </Typography>
                    </Grid>

                    <Grid item>
                        <Typography variant="body2">
                            {getDetectionTypes(props.uiAlert) || "-"}
                        </Typography>
                    </Grid>
                </Grid>
            )}

            {/* Description */}
            <Grid item xs={12}>
                <Grid item>
                    <Typography className={classes.fontMedium} variant="body2">
                        {t("label.description")}:
                    </Typography>
                </Grid>

                <Grid item>
                    <Typography variant="body2">
                        {props.uiAlert?.alert?.description || t("label.none")}
                    </Typography>
                </Grid>
            </Grid>

            {/* Subscription Groups */}
            <Grid item xs={6}>
                <Grid item>
                    <Typography className={classes.fontMedium} variant="body2">
                        {t("label.subscription-groups")}:
                    </Typography>
                </Grid>

                <Grid item>
                    <Typography variant="body2">
                        {props.uiAlert.subscriptionGroups.length > 0 &&
                            getAllSubscriptionGroupsName(props.uiAlert)}
                        {props.uiAlert.subscriptionGroups.length === 0 && (
                            <span>{t("label.none")}</span>
                        )}
                    </Typography>
                </Grid>
            </Grid>
        </Grid>
    );
};
