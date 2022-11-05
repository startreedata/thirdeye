/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { TypographyOptions } from "@material-ui/core/styles/createTypography";

// Material UI theme typography
export const typographyOptionsV1: TypographyOptions = {
    fontFamily: "Inter, sans-serif",
    fontWeightLight: 200,
    fontWeightRegular: 300,
    fontWeightMedium: 400,
    fontWeightBold: 500,
    h1: {
        fontSize: 96,
        fontWeight: 200,
    },
    h2: {
        fontSize: 60,
        fontWeight: 300,
    },
    h3: {
        fontSize: 48,
        fontWeight: 400,
    },
    h4: {
        fontSize: 36,
        fontWeight: 300,
    },
    h5: {
        fontSize: 24,
        fontWeight: 400,
    },
    h6: {
        fontSize: 16,
        fontWeight: 500,
    },
    subtitle1: {
        fontSize: 16,
        fontWeight: 400,
        lineHeight: "24px",
    },
    subtitle2: {
        fontSize: 14,
        fontWeight: 500,
        lineHeight: "20px",
    },
    body1: {
        fontSize: 18,
        fontWeight: 400,
        lineHeight: "28px",
    },
    body2: {
        fontSize: 14,
        fontWeight: 400,
        lineHeight: "20px",
    },
    button: {
        fontSize: 14,
        fontWeight: 500,
        lineHeight: "16px",
    },
    caption: {
        fontSize: 12,
        fontWeight: 400,
        lineHeight: "16px",
    },
    overline: {
        fontSize: 10,
        fontWeight: 500,
        lineHeight: "16px",
    },
};

export const codeTypographyOptionsV1 = {
    body2: {
        fontFamily: "Roboto Mono, monospace",
        fontSize: 14,
        fontWeight: 400,
        lineHeight: "20px",
    },
};
