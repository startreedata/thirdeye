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
import { makeStyles } from "@material-ui/core";
import { PaletteV1 } from "../../../../platform/utils";

export const useEnumerationItemRowV2Styles = makeStyles(() => ({
    name: {
        wordBreak: "break-all",
    },
    card: {
        borderLeft: "none",
        borderRight: "none",
        borderBottom: "none",
        borderRadius: 0,
    },
    reportMissedAnomalyButtonContainer: {
        flex: 1,
    },
    expandChartButton: {
        color: PaletteV1.TextColorGrey,
    },
}));
