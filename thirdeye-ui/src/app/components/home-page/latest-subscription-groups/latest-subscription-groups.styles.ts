/*
 * Copyright 2024 StarTree Inc
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
import { makeStyles } from "@material-ui/core";
import { ColorV1 } from "../../../platform/utils/material-ui/color.util";

export const useLatestSubscriptionGroupsStyles = makeStyles((theme) => ({
    statusCellContainer: {
        display: "flex",
        alignItems: "center",
    },
    healthyIcon: {
        color: theme.palette.success.main,
        marginRight: theme.spacing(1),
    },
    unhealthyIcon: {
        color: theme.palette.error.main,
        marginRight: theme.spacing(1),
    },
    tableRow: {
        height: 41,
    },
    title: {
        marginBottom: theme.spacing(1),
    },
    allAlertsLink: {
        color: ColorV1.Emerald,
        marginTop: theme.spacing(1),
        fontWeight: 500,
    },
    alertsLinkIcon: {
        marginLeft: theme.spacing(1),
        fontSize: theme.spacing(2),
        fontWeight: 500,
    },
}));
