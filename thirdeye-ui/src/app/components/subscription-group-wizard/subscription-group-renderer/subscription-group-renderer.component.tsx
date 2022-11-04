import { Box, Divider, Grid, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { specTypeToUIConfig } from "../groups-editor/groups-editor.utils";
import { SubscriptionGroupRendererProps } from "./subscription-group-renderer.interfaces";

export const SubscriptionGroupRenderer: FunctionComponent<SubscriptionGroupRendererProps> =
    (props: SubscriptionGroupRendererProps) => {
        const { t } = useTranslation();

        return (
            <Grid container justifyContent="flex-end">
                {/* Name */}
                <Grid item sm={2}>
                    <Typography variant="subtitle1">
                        <strong>{t("label.name")}</strong>
                    </Typography>
                </Grid>

                <Grid item sm={10}>
                    <Typography variant="body2">
                        {(props.subscriptionGroup &&
                            props.subscriptionGroup.name) ||
                            t("label.no-data-marker")}
                    </Typography>
                </Grid>

                {/* Schedule(or Cron) */}
                <Grid item sm={2}>
                    <Typography variant="subtitle1">
                        <strong>{t("label.schedule")}</strong>
                    </Typography>
                </Grid>

                <Grid item sm={10}>
                    <Typography variant="body2">
                        {(props.subscriptionGroup &&
                            props.subscriptionGroup.cron) ||
                            t("label.no-data-marker")}
                    </Typography>
                </Grid>

                {/* Subscribed alerts */}
                <Grid item sm={2}>
                    <Typography variant="subtitle1">
                        <strong>{t("label.subscribed-alerts")}</strong>
                    </Typography>
                </Grid>

                {/* No subscribed alerts */}
                {!props.subscriptionGroup ||
                    (isEmpty(props.subscriptionGroup.alerts) && (
                        <Grid item sm={10}>
                            <Typography variant="body2">
                                {t("label.no-data-marker")}
                            </Typography>
                        </Grid>
                    ))}

                {/* All subscribed alerts */}
                {props.subscriptionGroup &&
                    !isEmpty(props.subscriptionGroup.alerts) && (
                        <Grid item sm={10}>
                            {props.subscriptionGroup.alerts.map(
                                (alert, index) => (
                                    <Typography key={index} variant="body2">
                                        {alert.name}
                                    </Typography>
                                )
                            )}
                        </Grid>
                    )}

                {/* Subscribed emails */}
                {props.subscriptionGroup &&
                    props.subscriptionGroup.notificationSchemes.email &&
                    !isEmpty(
                        props.subscriptionGroup.notificationSchemes.email.to
                    ) && (
                        <>
                            <Grid item sm={2}>
                                <Typography variant="subtitle1">
                                    <strong>
                                        {t("label.subscribed-emails")}
                                    </strong>
                                </Typography>
                            </Grid>
                            <Grid item sm={10}>
                                {props.subscriptionGroup.notificationSchemes.email.to.map(
                                    (email, index) => (
                                        <Typography key={index} variant="body2">
                                            {email}
                                        </Typography>
                                    )
                                )}
                            </Grid>
                        </>
                    )}

                {props.subscriptionGroup && (
                    <>
                        <Grid item xs={12}>
                            <Box marginBottom={1}>
                                <Divider />
                            </Box>
                            <Typography variant="h5">
                                {t("label.channels")}
                            </Typography>
                        </Grid>
                        {props.subscriptionGroup.specs.map((spec) => {
                            const uiConfig = specTypeToUIConfig[spec.type];

                            return (
                                <>
                                    <Grid item sm={2}>
                                        <Typography variant="subtitle1">
                                            <strong>
                                                {t(
                                                    uiConfig.internationalizationString
                                                )}
                                            </strong>
                                        </Typography>
                                    </Grid>
                                    <Grid item sm={10}>
                                        {React.createElement(
                                            uiConfig.reviewComponent,
                                            {
                                                configuration: spec,
                                            }
                                        )}
                                    </Grid>
                                </>
                            );
                        })}

                        {props.subscriptionGroup.specs.length === 0 && (
                            <Grid item xs={12}>
                                {t("message.no-notifications-groups")}
                            </Grid>
                        )}
                    </>
                )}
            </Grid>
        );
    };
