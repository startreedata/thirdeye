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
import { makeStyles } from "@material-ui/core";
import { ColorV1 } from "../../../platform/utils/material-ui/color.util";

export const InvestigationStepsNavigationStyles = makeStyles((theme) => ({
    tabsMenu: {
        maxHeight: "fit-content",
        padding: theme.spacing(1),
        width: "100%",
    },
    tab: {
        maxHeight: "fit-content",
        padding: theme.spacing(1),
        "& > span": {
            textAlign: "start",
            alignItems: "start",
        },
    },
    selectedTab: {
        maxHeight: "fit-content",
        backgroundColor: ColorV1.Grey5,
        borderRadius: 8,
        "& > span": {
            textAlign: "start",
            alignItems: "start",
        },
    },
    selectedTabContainer: {
        display: "flex",
        width: "100%",
        color: ColorV1.Blue6,
        justifyContent: "space-between",
    },
}));
