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
import { useHTTPAction } from "../create-rest-action";
import { Dataset } from "../dto/dataset.interfaces";
import { Datasource } from "../dto/datasource.interfaces";
import {
    GetDatasource,
    GetDatasourceByName,
    GetDatasources,
    GetDatasourceStatus,
    GetRecommendedDatasources,
    GetStatusResponse,
    GetTablesForDatasourceByName,
} from "./datasources.interfaces";
import {
    getAllDatasources,
    getDatasourceByName as getDatasourceByNameREST,
    getDatasource as getDatasourceREST,
    getRecommenedDatasource as getRecommenedDatasourceREST,
    getStatusForDatasource,
    getTablesForDatasource,
} from "./datasources.rest";

export const useGetDatasourceStatus = (): GetDatasourceStatus => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<GetStatusResponse>(getStatusForDatasource);

    const getDatasourceStatus = (
        datasourceId: number
    ): Promise<GetStatusResponse | undefined> => {
        return makeRequest(datasourceId);
    };

    return {
        healthStatus: data,
        getDatasourceStatus,
        status,
        errorMessages,
        resetData,
    };
};

export const useGetDatasources = (): GetDatasources => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Datasource[]>(getAllDatasources);

    const getDatasources = (): Promise<Datasource[] | undefined> => {
        return makeRequest();
    };

    return {
        datasources: data,
        getDatasources,
        status,
        errorMessages,
        resetData,
    };
};

export const useGetDatasource = (): GetDatasource => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Datasource>(getDatasourceREST);

    const getDatasource = (id: number): Promise<Datasource | undefined> => {
        return makeRequest(id);
    };

    return {
        datasource: data,
        getDatasource,
        status,
        errorMessages,
        resetData,
    };
};

// Datasource may also be fetched by name
export const useGetDatasourceByName = (): GetDatasourceByName => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Datasource>(getDatasourceByNameREST);

    const getDatasourceByName = (
        name: string
    ): Promise<Datasource | undefined> => {
        return makeRequest(name);
    };

    return {
        datasource: data,
        getDatasourceByName,
        status,
        errorMessages,
        resetData,
    };
};

export const useGetTablesForDatasourceID = (): GetTablesForDatasourceByName => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Dataset[]>(getTablesForDatasource);

    const getTableForDatasourceID = (
        name: number
    ): Promise<Dataset[] | undefined> => {
        return makeRequest(name);
    };

    return {
        tables: data,
        getTableForDatasourceID,
        status,
        errorMessages,
        resetData,
    };
};

export const useGetRecommendedDatasources = (): GetRecommendedDatasources => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Datasource>(getRecommenedDatasourceREST);

    const getRecommendedDatasource = (): Promise<Datasource | undefined> => {
        return makeRequest(name);
    };

    return {
        recommendedDatasource: data,
        getRecommendedDatasource,
        status,
        errorMessages,
        resetData,
    };
};
