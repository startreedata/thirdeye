/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { ChipProps, IconButton } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import React from "react";
import { BorderV1 } from "./border.util";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Chip
export const chipClassesV1 = {
    root: {
        borderRadius: DimensionV1.BorderRadiusDefault,
    },
    outlined: {
        border: BorderV1.BorderDefault,
    },
};

// Material UI theme property overrides for Chip
export const chipPropsV1: Partial<ChipProps> = {
    variant: "outlined",
    deleteIcon: (
        <IconButton size="small">
            <CloseIcon color="action" fontSize="small" />
        </IconButton>
    ),
};
