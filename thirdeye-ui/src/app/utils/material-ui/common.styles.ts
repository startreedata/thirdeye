// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import { makeStyles } from "@material-ui/core";
import { Palette } from "./palette.util";

export const useCommonStyles = makeStyles((theme) => ({
    gridLimitation: {
        // Padding to avoid the effect of Material-UI Grid negative margins
        // https://material-ui.com/components/grid/#limitations
        padding: theme.spacing(1),
    },
    cardContentBottomPaddingRemoved: {
        "&:last-child": {
            paddingBottom: "0px !important",
        },
    },
    ellipsis: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    backdrop: {
        position: "fixed",
        top: 0,
        left: 0,
        height: "100%",
        width: "100%",
        flex: 1,
        backgroundColor: Palette.COLOR_BACKGROUND_BACKDROP,
        zIndex: theme.zIndex.drawer + 2, // Backdrop above app bar
    },
    decreased: {
        color: theme.palette.error.dark,
    },
    increased: {
        color: theme.palette.success.dark,
    },
}));
