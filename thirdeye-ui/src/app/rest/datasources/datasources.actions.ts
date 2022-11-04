import { useHTTPAction } from "../create-rest-action";
import { Datasource } from "../dto/datasource.interfaces";
import {
    GetDatasources,
    GetDatasourceStatus,
    GetStatusResponse,
} from "./datasources.interfaces";
import { getAllDatasources, getStatusForDatasource } from "./datasources.rest";

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
