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
import { Box, FormControlLabel, Snackbar, Switch } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import classNames from "classnames";
import { delay, isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNotificationProviderV1 } from "../notification-provider-v1/notification-provider-v1.component";
import { NotificationV1 } from "../notification-provider-v1/notification-provider-v1.interfaces";
import { NotificationDisplayV1Props } from "./notification-display-v1.interfaces";
import { useNotificationDisplayV1Styles } from "./notification-display-v1.styles";

export const NotificationDisplayV1: FunctionComponent<NotificationDisplayV1Props> =
    ({ className, ...otherProps }) => {
        const displayAfter = 500; // milliseconds
        const hideAfter = 10_000; // milliseconds
        const { t } = useTranslation();

        const notificationDisplayV1Classes = useNotificationDisplayV1Styles();
        const [notification, setNotification] = useState<NotificationV1 | null>(
            null
        );
        const { notifications, remove } = useNotificationProviderV1();
        const [showDetails, setShowDetails] = useState(false);

        useEffect(() => {
            // Notifications updated, render latest with a visible delay
            setNotification(null);

            if (isEmpty(notifications)) {
                return;
            }

            const notificationTimerId = delay(
                setNotification,
                displayAfter,
                notifications[0]
            );

            // Clear delay if notifications update
            return () => clearTimeout(notificationTimerId);
        }, [notifications]);

        const handleClose = (): void => {
            if (!notification) {
                return;
            }

            remove(notification);
        };

        return (
            <div
                {...otherProps}
                className={classNames(className, "notification-display-v1")}
                data-testId="notfication-container"
            >
                {notification && (
                    <Snackbar
                        open
                        anchorOrigin={{ vertical: "top", horizontal: "center" }}
                        autoHideDuration={hideAfter}
                        onClose={handleClose}
                    >
                        <Alert
                            className="notification-display-v1-notification"
                            classes={{
                                action: notification.nonDismissible
                                    ? notificationDisplayV1Classes.notificationActionHidden
                                    : notificationDisplayV1Classes.notificationActionVisible,
                                message:
                                    notificationDisplayV1Classes.snackBarContainer,
                            }}
                            severity={notification.type}
                            variant="standard"
                            onClose={handleClose}
                        >
                            {notification.message}
                            {notification.details ? (
                                <Box
                                    className={
                                        notificationDisplayV1Classes.switchContainer
                                    }
                                >
                                    <FormControlLabel
                                        classes={{
                                            root: notificationDisplayV1Classes.switchLabel,
                                            label: notificationDisplayV1Classes.switchLabelLabel,
                                        }}
                                        control={
                                            <Switch
                                                checked={showDetails}
                                                onChange={() =>
                                                    setShowDetails((d) => !d)
                                                }
                                            />
                                        }
                                        label={t("label.show-details")}
                                    />
                                    {showDetails && (
                                        <Box>{notification.details}</Box>
                                    )}
                                </Box>
                            ) : null}
                        </Alert>
                    </Snackbar>
                )}
            </div>
        );
    };
