/*
 * Copyright 2024 StarTree Inc
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

export const useAnalysisPeriodStyles = makeStyles({
    container: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        gap: "8px",
        color: "#3D5F82",
        fontSize: "14px",
    },
    rangeContainer: {
        display: "flex",
        gap: "4px",
    },
    range: {
        cursor: "pointer",
    },
    selected: {
        background: "#e1edff",
        fontWeight: "bold",
    },
});
