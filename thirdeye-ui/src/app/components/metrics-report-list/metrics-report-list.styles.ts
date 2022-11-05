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
import { makeStyles } from "@material-ui/core";
import { PaletteV1 } from "../../platform/utils";

export const useMetricsReportListStyles = makeStyles((theme) => ({
    fullWidthCell: {
        width: "100%",
    },
    tableHeader: {
        backgroundColor: PaletteV1.DataGridHeaderBackgroundColor,
        "&> th": {
            ...theme.typography.subtitle2,
            height: "39px",
            paddingBottom: 0,
            paddingTop: 0,
        },
    },
    expanded: {
        "&> th": {
            borderBottom: "none",
        },
        "&> td": {
            borderBottom: "none",
        },
    },
}));
