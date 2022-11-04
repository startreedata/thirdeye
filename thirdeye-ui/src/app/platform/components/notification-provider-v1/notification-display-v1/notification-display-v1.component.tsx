import { Snackbar } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import classNames from "classnames";
import { delay, isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useNotificationProviderV1 } from "../notification-provider-v1/notification-provider-v1.component";
import { NotificationV1 } from "../notification-provider-v1/notification-provider-v1.interfaces";
import { NotificationDisplayV1Props } from "./notification-display-v1.interfaces";
import { useNotificationDisplayV1Styles } from "./notification-display-v1.styles";

export const NotificationDisplayV1: FunctionComponent<NotificationDisplayV1Props> =
    ({ className, ...otherProps }) => {
        const notificationDisplayV1Classes = useNotificationDisplayV1Styles();
        const [notification, setNotification] = useState<NotificationV1 | null>(
            null
        );
        const { notifications, remove } = useNotificationProviderV1();

        useEffect(() => {
            // Notifications updated, render latest with a visible delay
            setNotification(null);

            if (isEmpty(notifications)) {
                return;
            }

            const notificationTimerId = delay(
                setNotification,
                100,
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
            >
                {notification && (
                    <Snackbar
                        open
                        anchorOrigin={{ vertical: "top", horizontal: "center" }}
                    >
                        <Alert
                            className="notification-display-v1-notification"
                            classes={{
                                action: notification.nonDismissible
                                    ? notificationDisplayV1Classes.notificationActionHidden
                                    : notificationDisplayV1Classes.notificationActionVisible,
                            }}
                            severity={notification.type}
                            onClose={handleClose}
                        >
                            {notification.message}
                        </Alert>
                    </Snackbar>
                )}
            </div>
        );
    };
