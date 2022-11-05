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
import { BorderV1 } from "./border.util";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Table Pagination
export const tablePaginationClassesV1 = {
    root: {
        borderTop: BorderV1.BorderDefault,
    },
    toolbar: {
        height: DimensionV1.ToolbarHeight,
    },
};
