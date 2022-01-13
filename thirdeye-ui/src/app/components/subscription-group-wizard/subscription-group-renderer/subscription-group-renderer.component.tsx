import { Grid, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SubscriptionGroupRendererProps } from "./subscription-group-renderer.interfaces";

export const SubscriptionGroupRenderer: FunctionComponent<SubscriptionGroupRendererProps> = (
    props: SubscriptionGroupRendererProps
) => {
    const { t } = useTranslation();

    return (
        <Grid container item justify="flex-end">
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
                    <strong>{t("label.cron")}</strong>
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
                        {props.subscriptionGroup.alerts.map((alert, index) => (
                            <Typography key={index} variant="body2">
                                {alert.name}
                            </Typography>
                        ))}
                    </Grid>
                )}

            {/* Subscribed emails */}
            <Grid item sm={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.subscribed-emails")}</strong>
                </Typography>
            </Grid>

            {/* No subscribed emails */}
            {!props.subscriptionGroup ||
                !props.subscriptionGroup.notificationSchemes.email ||
                (isEmpty(
                    props.subscriptionGroup.notificationSchemes.email.to
                ) && (
                    <Grid item sm={10}>
                        <Typography variant="body2">
                            {t("label.no-data-marker")}
                        </Typography>
                    </Grid>
                ))}

            {/* All subscribed emails */}
            {props.subscriptionGroup &&
                props.subscriptionGroup.notificationSchemes.email &&
                !isEmpty(
                    props.subscriptionGroup.notificationSchemes.email.to
                ) && (
                    <Grid item sm={10}>
                        {props.subscriptionGroup.notificationSchemes.email.to.map(
                            (email, index) => (
                                <Typography key={index} variant="body2">
                                    {email}
                                </Typography>
                            )
                        )}
                    </Grid>
                )}
        </Grid>
    );
};
