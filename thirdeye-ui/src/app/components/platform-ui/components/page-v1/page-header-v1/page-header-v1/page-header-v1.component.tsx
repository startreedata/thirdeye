// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Toolbar } from "@material-ui/core";
import classNames from "classnames";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { NotificationDisplayV1 } from "../../../notification-provider-v1/notification-display-v1/notification-display-v1.component";
import { useNotificationProviderV1 } from "../../../notification-provider-v1/notification-provider-v1/notification-provider-v1.component";
import { usePageV1 } from "../../page-v1/page-v1.component";
import { PageHeaderV1Props } from "./page-header-v1.interfaces";
import { usePageHeaderV1Styles } from "./page-header-v1.styles";

export const PageHeaderV1: FunctionComponent<PageHeaderV1Props> = ({
    notificationsOnly,
    className,
    children,
    ...otherProps
}: PageHeaderV1Props) => {
    const pageHeaderV1Classes = usePageHeaderV1Styles();
    const { notifications } = useNotificationProviderV1();
    const { setHeaderVisible } = usePageV1();

    useEffect(() => {
        // Set header visibility
        setHeaderVisible(!notificationsOnly);
    }, [notificationsOnly]);

    return (
        <>
            {/* Header */}
            <Toolbar
                {...otherProps}
                className={classNames(
                    pageHeaderV1Classes.pageHeader,
                    {
                        [pageHeaderV1Classes.pageHeaderHidden]: notificationsOnly,
                    },
                    className,
                    "page-header-v1"
                )}
                classes={{ gutters: pageHeaderV1Classes.pageHeaderGutters }}
            >
                {children}
            </Toolbar>

            {/* Notifications */}
            <div
                className={classNames(
                    pageHeaderV1Classes.pageNotifications,
                    isEmpty(notifications)
                        ? pageHeaderV1Classes.pageNotificationsMinimized
                        : pageHeaderV1Classes.pageNotificationsMaximized,
                    notificationsOnly
                        ? pageHeaderV1Classes.pageNotificationsWithoutHeader
                        : pageHeaderV1Classes.pageNotificationsWithHeader,
                    "page-header-v1-notifications"
                )}
            >
                <NotificationDisplayV1 className="page-header-v1-notification-display" />
            </div>
        </>
    );
};
