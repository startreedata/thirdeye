// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { BorderV1 } from "./border.util";
import { PaletteV1 } from "./palette.util";

// Material UI theme style overrides for OutlinedInput
export const outlinedInputClassesV1 = {
    notchedOutline: {
        border: BorderV1.BorderDefault,
        borderColor: PaletteV1.BorderColorDefault,
    },
    adornedStart: {
        paddingLeft: 8,
    },
    adornedEnd: {
        paddingRight: 8,
    },
};
