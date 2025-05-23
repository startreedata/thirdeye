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
import { DimensionV1 } from "../../../utils/material-ui/dimension.util";

export const useTileButtonV1Styles = makeStyles((theme) => ({
    tileButton: {
        height: 200,
        width: 240,
        backgroundColor: theme.palette.background.paper,
        borderRadius: DimensionV1.CardBorderRadius,
        padding: theme.spacing(3),
    },
    tileButtonLabel: {
        height: "100%",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        textTransform: "none",
    },
}));
