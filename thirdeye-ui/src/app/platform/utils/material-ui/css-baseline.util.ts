// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import {
    interExtraLight200,
    interLight300,
    interMedium500,
    interRegular400,
    robotoMonoRegular400,
} from "./font.util";

// Material UI theme style overrides for CssBaseline
export const cssBaselineClassesV1 = {
    "@global": {
        "@font-face": [
            interExtraLight200,
            interLight300,
            interRegular400,
            interMedium500,
            robotoMonoRegular400,
        ],
    },
};
