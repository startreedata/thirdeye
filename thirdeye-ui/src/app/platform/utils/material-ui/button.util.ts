// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { ButtonGroupProps, ButtonProps } from "@material-ui/core";
import { BorderV1 } from "./border.util";

// Material UI theme style overrides for Button
export const buttonClassesV1 = {
    contained: {
        border: BorderV1.BorderDefault,
    },
    outlined: {
        border: BorderV1.BorderDefault,
    },
    containedSizeLarge: {
        paddingTop: 8,
        paddingBottom: 8,
        paddingLeft: 16,
        paddingRight: 16,
    },
    outlinedSizeLarge: {
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
