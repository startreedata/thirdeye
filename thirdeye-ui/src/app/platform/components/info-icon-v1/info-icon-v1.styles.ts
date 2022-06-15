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

// Copyright 2022 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { DimensionV1 } from "../../utils/material-ui/dimension.util";

export const useInfoIconV1Styles = makeStyles((theme) => ({
    infoIconFlex: {
        display: "flex",
    },
    infoIconInline: {
        display: "inline-block",
    },
    infoIconPadding: {
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
    },
    info: {
        padding: theme.spacing(1),
    },
    autoFitToContents: {
        minWidth: "auto",
    },
    defaultSizing: {
        maxWidth: DimensionV1.MenuWidth * 2,
    },
}));
