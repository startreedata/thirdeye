// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ButtonGroupProps, ButtonProps } from "@material-ui/core";
import { BorderV1 } from "./border.util";

// Material UI theme style overrides for Button
export const buttonClassesV1 = {
    contained: {
        border: BorderV1.BorderDefault,
    },
    containedSizeLarge: {
        paddingTop: 8,
        paddingBottom: 8,
        paddingLeft: 16,
        paddingRight: 16,
    },
    iconSizeLarge: {
        "&>*:first-child": {
            fontSize: 24,
            marginTop: -4,
            marginBottom: -4,
        },
    },
};

// Material UI theme property overrides for Button
export const buttonPropsV1: Partial<ButtonProps> = {
    disableElevation: true,
    size: "large",
    variant: "contained",
};

export const buttonGroupPropsV1: Partial<ButtonGroupProps> = {
    disableElevation: true,
    size: "large",
    variant: "contained",
};
