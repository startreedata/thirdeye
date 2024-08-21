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

export const usageInsightsStyle = makeStyles({
    sectionHeading: {
        display: "flex",
        justifyContent: "space-between",
    },
    tableHeading: {
        padding: "8px 16px",
        border: "1px solid rgb(158, 158, 158)",
        borderBottom: 0,
        borderTopLeftRadius: "16px",
        borderTopRightRadius: "16px",
        color: "#0B263E",
        fontSize: "14px",
        fontWeight: 700,
    },
    table: {
        border: "1px solid rgb(158, 158, 158)",
        borderBottomLeftRadius: "16px",
        borderBottomRightRadius: "16px",
    },
    tablesContainer: {
        display: "flex",
        gap: "10px",
        "& >div": {
            flex: 1,
        },
    },
});
