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
import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui/palette.util";

export const useNameValueDisplayCardStyles = makeStyles((theme) => ({
    nameValueDisplayCard: {
        height: "100%",
        borderColor: Palette.COLOR_BORDER_DEFAULT,
    },
    nameValueDisplayCardContent: {
        "&:last-child": {
            paddingBottom: theme.spacing(2),
        },
    },
    list: {
        maxHeight: 90,
        overflowY: "auto",
    },
    listItem: {
        padding: 0,
    },
    listItemText: {
        marginTop: 2,
        marginBottom: 2,
    },
}));
