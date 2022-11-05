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
import { DimensionV1 } from "./dimension.util";
import { PaletteV1 } from "./palette.util";

// Material UI theme borders
export const BorderV1 = {
    TabBorderSelected: `${DimensionV1.TabBorderWidthSelected}px solid`,
    BorderDefault: `${DimensionV1.BorderWidthDefault}px solid ${PaletteV1.BorderColorDefault}`,
    BorderSecondary: `${DimensionV1.BorderWidthDefault}px solid ${PaletteV1.SecondaryColorMain}`,
} as const;
