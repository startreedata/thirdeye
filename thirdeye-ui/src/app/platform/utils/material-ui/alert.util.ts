// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { AlertProps } from "@material-ui/lab";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Alert
export const alertClassesV1 = {
    root: {
        borderRadius: DimensionV1.CardBorderRadius,
        // Adjusted padding so that default alert height is DimensionV1.AlertHeight
        paddingTop: 2,
        paddingBottom: 2,
    },
    filledWarning: {
        color: "inherit",
    },
    action: {
        display: "flex",
        alignItems: "flex-start",
        paddingTop: 6,
    },
};

// Material UI theme property overrides for Alert
export const alertPropsV1: Partial<AlertProps> = {
    icon: false,
    variant: "filled",
};
