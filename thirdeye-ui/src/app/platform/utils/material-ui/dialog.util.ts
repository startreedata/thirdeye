// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { BorderV1 } from "./border.util";
import { DimensionV1 } from "./dimension.util";
import { typographyOptionsV1 } from "./typography.util";

// Material UI theme style overrides for Dialog
export const dialogClassesV1 = {
    paper: {
        border: BorderV1.BorderDefault,
        borderRadius: DimensionV1.CardBorderRadius,
    },
};

export const dialogContentClassesV1 = {
    root: {
        ...typographyOptionsV1.body2,
        paddingTop: DimensionV1.CardContentPadding,
        paddingBottom: DimensionV1.CardContentPadding,
    },
};

export const dialogActionsClassesV1 = {
    root: {
        borderTop: BorderV1.BorderDefault,
        padding: DimensionV1.DialogActionsPadding,
    },
    spacing: {
        "&>:not(:first-child)": {
            marginLeft: DimensionV1.DialogActionsPadding,
        },
    },
};
