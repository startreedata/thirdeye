// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { TextFieldProps } from "@material-ui/core";

// Material UI theme property overrides for TextField
export const textFieldPropsV1: Partial<TextFieldProps> = {
    size: "small",
    variant: "outlined",
    InputLabelProps: {
        style: {
            backgroundColor: "white",
            paddingRight: "5px",
        },
    },
};
