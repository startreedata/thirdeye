import axios from "axios";
import { Dataset } from "../dto/dataset.interfaces";

const BASE_URL_DATASETS = "/api/datasets";
const BASE_URL_DATASOURCE = "/api/data-sources";

export const getDataset = async (id: number): Promise<Dataset> => {
    const response = await axios.get(`${BASE_URL_DATASETS}/${id}`);

    return response.data;
};

export const getAllDatasets = async (): Promise<Dataset[]> => {
    const response = await axios.get(BASE_URL_DATASETS);

    return response.data;
};

export const createDataset = async (dataset: Dataset): Promise<Dataset> => {
    const response = await axios.post(BASE_URL_DATASETS, [dataset]);

    return response.data[0];
};

export const createDatasets = async (
    datasets: Dataset[]
): Promise<Dataset[]> => {
    const response = await axios.post(BASE_URL_DATASETS, datasets);

    return response.data;
};

export const onBoardDataset = async (
    datasetName: string,
    dataSourceName: string
): Promise<Dataset> => {
    const response = await axios.post(
        `${BASE_URL_DATASOURCE}/onboard-dataset/`,
        new URLSearchParams({
            dataSourceName,
            datasetName,
        })
    );

    return response.data;
};

export const updateDataset = async (dataset: Dataset): Promise<Dataset> => {
    const response = await axios.put(BASE_URL_DATASETS, [dataset]);

    return response.data[0];
};

export const updateDatasets = async (
    datasets: Dataset[]
): Promise<Dataset[]> => {
    const response = await axios.put(BASE_URL_DATASETS, datasets);

    return response.data;
};

export const deleteDataset = async (id: number): Promise<Dataset> => {
    const response = await axios.delete(`${BASE_URL_DATASETS}/${id}`);

    return response.data;
};
