///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui/palette.util";

export const useTooltipWithBoundsStyles = makeStyles(() => ({
    tooltipContainer: {
        position: "relative",
    },
    tooltip: {
        color: `${Palette.COLOR_FONT_TOOLTIP} !important`,
        zIndex: 999,
        filter: "drop-shadow(rgba(0, 0, 0, 0.5) 0 2px 10px)",
    },
}));
