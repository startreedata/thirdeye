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
import { makeStyles } from "@material-ui/core";
import { BorderV1 } from "../../../../utils/material-ui/border.util";
import { DimensionV1 } from "../../../../utils/material-ui/dimension.util";

export const usePageHeaderV1Styles = makeStyles((theme) => ({
    pageHeader: {
        position: "sticky",
        top: 0,
        minHeight: DimensionV1.PageHeaderHeight,
        maxHeight: DimensionV1.PageHeaderHeight,
        backgroundColor: theme.palette.background.paper,
        borderBottom: BorderV1.BorderDefault,
        zIndex: theme.zIndex.appBar,
    },
    pageHeaderHidden: {
        minHeight: 0,
        maxHeight: 0,
    },
    pageHeaderGutters: {
        paddingLeft: theme.spacing(DimensionV1.PageGridSpacing),
        paddingRight: theme.spacing(DimensionV1.PageGridSpacing),
    },
    pageNotifications: {
        position: "sticky",
        backgroundColor: theme.palette.background.default,
        paddingLeft: theme.spacing(DimensionV1.PageGridSpacing),
        paddingRight: theme.spacing(DimensionV1.PageGridSpacing),
        zIndex: theme.zIndex.appBar,
    },
    pageNotificationsWithoutHeader: {
        top: 0,
    },
    pageNotificationsWithHeader: {
        top: DimensionV1.PageHeaderHeight,
    },
    pageNotificationsMinimized: {
        minHeight: theme.spacing(DimensionV1.PageGridSpacing),
        paddingTop: theme.spacing(DimensionV1.PageGridSpacing / 2),
        paddingBottom: theme.spacing(DimensionV1.PageGridSpacing / 2),
        zIndex: -1,
        transition: theme.transitions.create(["min-height", "z-index"], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    pageNotificationsMaximized: {
        minHeight:
            DimensionV1.AlertHeight +
            theme.spacing(DimensionV1.PageGridSpacing), // Default height of an alert and top and bottom padding
        paddingTop: theme.spacing(DimensionV1.PageGridSpacing / 2),
        paddingBottom: theme.spacing(DimensionV1.PageGridSpacing / 2),
        zIndex: theme.zIndex.appBar,
        transition: theme.transitions.create(["min-height", "z-index"], {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
}));
