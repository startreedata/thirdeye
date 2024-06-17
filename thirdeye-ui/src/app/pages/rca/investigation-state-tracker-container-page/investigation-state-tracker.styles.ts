/*
 * Copyright 2023 StarTree Inc
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
import { createTheme, makeStyles } from "@material-ui/core";
import { lightV1 } from "../../../platform/utils";
import { ColorV1 } from "../../../platform/utils/material-ui/color.util";

export const InvestigationStateTrackerStyles = makeStyles(() => ({
    roundedCard: {
        borderRadius: "8px",
    },
}));

export const InvestigationStateTrackerTheme = createTheme({
    ...lightV1,
    palette: {
        primary: {
            main: ColorV1.Blue6,
        },
    },
    overrides: {
        ...lightV1.overrides,
        MuiButton: {
            contained: {
                borderRadius: "16px",
            },
            outlined: {
                borderRadius: "16px",
            },
        },
    },
});
