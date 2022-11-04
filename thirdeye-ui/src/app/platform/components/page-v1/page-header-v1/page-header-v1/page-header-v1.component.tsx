// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
}) => {
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
                        [pageHeaderV1Classes.pageHeaderHidden]:
                            notificationsOnly,
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
