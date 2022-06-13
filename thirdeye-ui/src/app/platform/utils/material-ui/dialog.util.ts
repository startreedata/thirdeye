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
