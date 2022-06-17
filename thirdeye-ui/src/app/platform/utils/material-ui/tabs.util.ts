///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { TabsProps } from "@material-ui/core";
import { BorderV1 } from "./border.util";
import { DimensionV1 } from "./dimension.util";
import { typographyOptionsV1 } from "./typography.util";

// Material UI theme style overrides for Tabs
export const tabsClassesV1 = {
    root: {
        minHeight: DimensionV1.TabHeight,
        maxHeight: DimensionV1.TabHeight,
    },
};

export const tabClassesV1 = {
    root: {
        minHeight: DimensionV1.TabHeight,
        maxHeight: DimensionV1.TabHeight,
        maxWidth: "none",
        alignItems: "flex-start",
        "text-transform": "none",
        ...typographyOptionsV1.subtitle1,
        paddingTop: 4,
        "&$selected": {
            ...typographyOptionsV1.h6,
            borderBottom: BorderV1.TabBorderSelected,
        },
    },
    textColorInherit: {
        opacity: 1,
    },
};

// Material UI theme property overrides for Tabs
export const tabsPropsV1: Partial<TabsProps> = {
    scrollButtons: "off",
    TabIndicatorProps: {
        hidden: true, // Default selected tab indicator is not responsive
    },
    variant: "scrollable",
};
