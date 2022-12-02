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
import { useHTTPAction } from "../create-rest-action";
import { Dataset } from "../dto/dataset.interfaces";
import { Datasource } from "../dto/datasource.interfaces";
import {
    GetDatasource,
    GetDatasourceByName,
    GetDatasources,
    GetDatasourceStatus,
    GetStatusResponse,
    GetTablesForDatasourceByName,
} from "./datasources.interfaces";
import {
    getAllDatasources,
    getDatasource as getDatasourceREST,
    getDatasourceByName as getDatasourceByNameREST,
    getStatusForDatasource,
    getTablesForDatasource,
} from "./datasources.rest";

export const useGetDatasourceStatus = (): GetDatasourceStatus => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<GetStatusResponse>(getStatusForDatasource);

    const getDatasourceStatus = (
        datasourceName: string
    ): Promise<GetStatusResponse | undefined> => {
        return makeRequest(datasourceName);
    };

    return { healthStatus: data, getDatasourceStatus, status, errorMessages };
};

export const useGetDatasources = (): GetDatasources => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Datasource[]>(getAllDatasources);

    const getDatasources = (): Promise<Datasource[] | undefined> => {
        return makeRequest();
    };

    return { datasources: data, getDatasources, status, errorMessages };
};

export const useGetDatasource = (): GetDatasource => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Datasource>(getDatasourceREST);

    const getDatasource = (id: number): Promise<Datasource | undefined> => {
        return makeRequest(id);
    };

    return { datasource: data, getDatasource, status, errorMessages };
};

// Datasource may also be fetched by name
export const useGetDatasourceByName = (): GetDatasourceByName => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Datasource>(getDatasourceByNameREST);

    const getDatasourceByName = (
        name: string
    ): Promise<Datasource | undefined> => {
        return makeRequest(name);
    };

    return { datasource: data, getDatasourceByName, status, errorMessages };
};

export const useGetTablesForDatasourceName =
    (): GetTablesForDatasourceByName => {
        const { data, makeRequest, status, errorMessages } = useHTTPAction<
            Dataset[]
        >(getTablesForDatasource);

        const getTableForDatasourceName = (
            name: string
        ): Promise<Dataset[] | undefined> => {
            return makeRequest(name);
        };

        return {
            tables: data,
            getTableForDatasourceName,
            status,
            errorMessages,
        };
    };
