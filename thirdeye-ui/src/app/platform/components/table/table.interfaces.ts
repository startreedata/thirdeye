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
import { ReactElement } from "react";

export interface TableColumns {
    datakey: string;
    title: string;
    customRender?: (data: TableRow) => unknown;
}

export interface TableRow {
    [datakey: string]: unknown;
}
export interface TableProps {
    data: TableRow[];
    columns: TableColumns[];
    emptyStateView?: ReactElement | string;
}
