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

export const useAlgorithmRowExpandedStyles = makeStyles((theme) => ({
    /**
     * Make the row on top of the one with expanded content blend by removing
     * the bottom border
     */
    root: {
        "& > *": {
            borderBottom: "unset",
        },
    },
    expandedRowParent: {
        backgroundColor: "rgb(241,242,255)",
    },
    expandedRow: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        backgroundColor: "rgba(247, 249, 255)",
    },
    closedRow: {
        padding: 0,
    },
}));
