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
import { darken, lighten } from "@material-ui/core/styles/colorManipulator";

export const useRCACHartLegendStyles = makeStyles((theme) => ({
    infoAlert: {
        backgroundColor: lighten(theme.palette.primary.light, 0.9),
        color: darken(theme.palette.primary.light, 0.5),
    },
}));
