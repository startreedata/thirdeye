import axios from "axios";
import { Dataset } from "../dto/dataset.interfaces";
import { Datasource } from "../dto/datasource.interfaces";

const BASE_URL_DATASOURCES = "/api/data-sources";

export const getDatasource = async (id: number): Promise<Datasource> => {
    const response = await axios.get(`${BASE_URL_DATASOURCES}/${id}`);

    return response.data;
};

export const getAllDatasources = async (): Promise<Datasource[]> => {
    const response = await axios.get(BASE_URL_DATASOURCES);

    return response.data;
};

export const createDatasource = async (
    datasource: Datasource
): Promise<Datasource> => {
    const response = await axios.post(BASE_URL_DATASOURCES, [datasource]);

    return response.data[0];
};

export const createDatasources = async (
    datasources: Datasource[]
): Promise<Datasource[]> => {
    const response = await axios.post(BASE_URL_DATASOURCES, datasources);

    return response.data;
};

export const updateDatasource = async (
    datasource: Datasource
): Promise<Datasource> => {
    const response = await axios.put(BASE_URL_DATASOURCES, [datasource]);

    return response.data[0];
};

export const updateDatasources = async (
    datasources: Datasource[]
): Promise<Datasource[]> => {
    const response = await axios.put(BASE_URL_DATASOURCES, datasources);

    return response.data;
};

export const onboardAllDatasets = async (
    datasourceName: string
): Promise<Dataset[]> => {
    const response = await axios.post(`${BASE_URL_DATASOURCES}/onboard-all`, {
        name: datasourceName,
    });

    return response.data;
};

export const deleteDatasource = async (id: number): Promise<Datasource> => {
    const response = await axios.delete(`${BASE_URL_DATASOURCES}/${id}`);

    return response.data;
};
