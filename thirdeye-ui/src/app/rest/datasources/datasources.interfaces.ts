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
import { ActionHook } from "../actions.interfaces";
import { Dataset } from "../dto/dataset.interfaces";
import { Datasource } from "../dto/datasource.interfaces";

export interface GetStatusResponse {
    code?: string;
    list?: {
        code: string;
        msg: string;
    }[];
}

export interface GetDatasourceStatus extends ActionHook {
    healthStatus: GetStatusResponse | null;
    getDatasourceStatus: (
        datasourceId: number
    ) => Promise<GetStatusResponse | undefined>;
}

export interface GetDatasources extends ActionHook {
    datasources: Datasource[] | null;
    getDatasources: () => Promise<Datasource[] | undefined>;
}

export interface GetDatasource extends ActionHook {
    datasource: Datasource | null;
    getDatasource: (id: number) => Promise<Datasource | undefined>;
}

export interface GetDatasourceByName extends ActionHook {
    datasource: Datasource | null;
    getDatasourceByName: (name: string) => Promise<Datasource | undefined>;
}

export interface GetTablesForDatasourceByName extends ActionHook {
    tables: Dataset[] | null;
    getTableForDatasourceID: (name: number) => Promise<Dataset[] | undefined>;
}
