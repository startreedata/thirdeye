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
import { Dimension } from "./dimension.util";
import { Palette } from "./palette.util";

// Material-UI theme borders
export const Border = {
    BORDER_DEFAULT: `${Dimension.WIDTH_BORDER_DEFAULT}px solid ${Palette.COLOR_BORDER_DEFAULT}`,
    BORDER_INPUT_DEFAULT: `${Dimension.WIDTH_BORDER_DEFAULT}px solid ${Palette.COLOR_BORDER_INPUT_DEFAULT}`,
    BORDER_INPUT_ERROR: `${Dimension.WIDTH_BORDER_DEFAULT}px solid ${Palette.COLOR_BORDER_INPUT_ERROR}`,
} as const;
