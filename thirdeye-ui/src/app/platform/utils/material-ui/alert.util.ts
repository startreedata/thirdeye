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
import { AlertProps } from "@material-ui/lab";
import { allowedAppQueryStringKeys } from "../../../utils/params/params.util";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Alert
export const alertClassesV1 = {
    root: {
        borderRadius: DimensionV1.CardBorderRadius,
        // Adjusted padding so that default alert height is DimensionV1.AlertHeight
        paddingTop: 2,
        paddingBottom: 2,
    },
    filledWarning: {
        color: "inherit",
    },
    action: {
        display: "flex",
        alignItems: "flex-start",
        paddingTop: 6,
    },
};

// Material UI theme property overrides for Alert
export const alertPropsV1: Partial<AlertProps> = {
    icon: false,
    variant: "filled",
};
export const getIsValidTimeRange = (searchParams: URLSearchParams): boolean => {
    let queryPresent = false;
    for (const allowedAppQueryStringKey of allowedAppQueryStringKeys) {
        if (searchParams.has(allowedAppQueryStringKey)) {
            queryPresent = Boolean(searchParams.get(allowedAppQueryStringKey));
        } else {
            queryPresent = false;

            break;
        }
    }

    return queryPresent;
};
