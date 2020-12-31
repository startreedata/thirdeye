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
        <Grid container justify="flex-end">
            {/* Name */}
            <Grid item md={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.name")}</strong>
                </Typography>
            </Grid>

            <Grid item md={10}>
                <Typography variant="body1">
                    {(props.subscriptionGroup &&
                        props.subscriptionGroup.name) ||
                        t("label.no-data-available-marker")}
                </Typography>
            </Grid>

            {/* Subscribed alerts */}
            <Grid item md={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.subscribed-alerts")}</strong>
                </Typography>
            </Grid>

            {/* No subscribed alerts */}
            {!props.subscriptionGroup ||
                (isEmpty(props.subscriptionGroup.alerts) && (
                    <Grid item md={10}>
                        <Typography variant="body1">
                            {t("label.no-data-available-marker")}
                        </Typography>
                    </Grid>
                ))}

            {/* All subscribed alerts */}
            {props.subscriptionGroup &&
                !isEmpty(props.subscriptionGroup.alerts) && (
                    <Grid item md={10}>
                        {props.subscriptionGroup.alerts.map((alert, index) => (
                            <Typography key={index} variant="body1">
                                {alert.name}
                            </Typography>
                        ))}
                    </Grid>
                )}

            {/* Subscribed emails */}
            <Grid item md={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.subscribed-emails")}</strong>
                </Typography>
            </Grid>

            {/* No subscribed emails */}
            {!props.subscriptionGroup ||
                !props.subscriptionGroup.emailSettings ||
                (isEmpty(props.subscriptionGroup.emailSettings.to) && (
                    <Grid item md={10}>
                        <Typography variant="body1">
                            {t("label.no-data-available-marker")}
                        </Typography>
                    </Grid>
                ))}

            {/* All subscribed emails */}
            {props.subscriptionGroup &&
                props.subscriptionGroup.emailSettings &&
                !isEmpty(props.subscriptionGroup.emailSettings.to) && (
                    <Grid item md={10}>
                        {props.subscriptionGroup.emailSettings.to.map(
                            (email, index) => (
                                <Typography key={index} variant="body1">
                                    {email}
                                </Typography>
                            )
                        )}
                    </Grid>
                )}
        </Grid>
    );
};
