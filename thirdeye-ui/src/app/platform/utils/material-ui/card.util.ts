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
import { CardProps } from "@material-ui/core";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Card
export const cardClassesV1 = {
    root: {
        borderRadius: DimensionV1.CardBorderRadius,
    },
};

export const cardContentClassesV1 = {
    root: {
        padding: DimensionV1.CardContentPadding,
        "&:last-child": {
            paddingBottom: DimensionV1.CardContentPadding,
        },
    },
};

// Material UI theme property overrides for Card
export const cardPropsV1: Partial<CardProps> = {
    elevation: 0,
};
