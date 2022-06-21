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
// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Fade } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import classNames from "classnames";
import { delay, isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useNotificationProviderV1 } from "../notification-provider-v1/notification-provider-v1.component";
import { NotificationV1 } from "../notification-provider-v1/notification-provider-v1.interfaces";
import { NotificationDisplayV1Props } from "./notification-display-v1.interfaces";
import { useNotificationDisplayV1Styles } from "./notification-display-v1.styles";

export const NotificationDisplayV1: FunctionComponent<
    NotificationDisplayV1Props
> = ({ className, ...otherProps }) => {
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
                <Fade in={Boolean(notification)} timeout={{ enter: 1000 }}>
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
                </Fade>
            )}
        </div>
    );
};
