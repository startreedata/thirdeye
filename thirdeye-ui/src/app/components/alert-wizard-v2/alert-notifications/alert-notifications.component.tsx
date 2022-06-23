import { Box, Grid, Switch, Typography } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { AlertNotificationsProps } from "./alert-notifications.interfaces";
import { SubscriptionGroups } from "./subscription-groups/subscription-groups.component";

export const AlertNotifications: FunctionComponent<AlertNotificationsProps> = ({
    alert,
    onSubscriptionGroupsChange,
}) => {
    const { t } = useTranslation();
    const [isNotificationsOn, setIsNotificationsOn] = useState(false);

    return (
        <PageContentsCardV1 fullHeight>
            <Grid container>
                <Grid container item xs={12}>
                    <Grid item lg={3} md={5} sm={10} xs={10}>
                        <Box marginBottom={2}>
                            <Typography variant="h5">
                                {t("label.create-notifications")}
                            </Typography>
                            <Typography variant="body2">
                                {t(
                                    "message.setup-notifications-for-your-subscription-groups"
                                )}
                            </Typography>
                        </Box>
                    </Grid>
                    <Grid item lg={9} md={7} sm={2} xs={2}>
                        <Switch
                            checked={isNotificationsOn}
                            color="primary"
                            name="checked"
                            onChange={() =>
                                setIsNotificationsOn(!isNotificationsOn)
                            }
                        />
                    </Grid>
                </Grid>

                {isNotificationsOn && (
                    <SubscriptionGroups
                        alert={alert}
                        onSubscriptionGroupsChange={onSubscriptionGroupsChange}
                    />
                )}
            </Grid>
        </PageContentsCardV1>
    );
};
