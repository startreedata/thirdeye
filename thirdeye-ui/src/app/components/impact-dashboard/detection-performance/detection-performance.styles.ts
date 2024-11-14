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

export const useDetectionPerformanceStyles = makeStyles({
    sectionHeading: {
        display: "flex",
        justifyContent: "space-between",
    },
    visualizationContainer: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "10px",
    },
    alertAndRange: {
        display: "flex",
        gap: "10px",
    },
    select: {
        width: "250px",
        borderRadius: "6px",
        "& .MuiOutlinedInput-input": {
            padding: "0 12px",
        },
    },
    tooltip: {
        display: "flex",
        flexDirection: "column",
        gap: "10px",
        padding: "8px",
        "& .currentPeriod": {
            color: "#006CA7",
        },
        "& .previousPeriod": {
            color: "#F37B0E",
        },
    },
});
