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
import { ColorV1 } from "../../../../../../platform/utils/material-ui/color.util";

export const multipleDimensionStyle = makeStyles((theme) => ({
    textArea: {
        width: "100%",
        borderRadius: "8px 8px 0 0",
        paddingTop: "5px",
        paddingLeft: theme.spacing(1),
    },
    textAreaContainer: {
        gap: 0,
        display: "flex",
        flexDirection: "column",
    },
    footer: {
        border: `1px solid ${ColorV1.Grey10}`,
        borderRadius: "0 0 8px 8px",
        overflow: "hidden",
        backgroundColor: ColorV1.Grey9,
        padding: "8px 12px",
        display: "flex",

        "& > button": {
            backgroundColor: ColorV1.White1,
            border: `1px solid ${ColorV1.Grey10}`,
            borderRadius: 8,
        },
    },
    card: {
        borderRadius: "8px",
        border: "1px solid #B4CDE0",
        padding: "24px",
        width: "100%",
        alignItems: "center",
    },
}));
