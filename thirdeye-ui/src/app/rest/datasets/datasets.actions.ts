import { useHTTPAction } from "../create-rest-action";
import { Dataset } from "../dto/dataset.interfaces";
import { GetDataset, GetDatasets } from "./dataset.interfaces";
import { getAllDatasets, getDataset as getDatasetREST } from "./datasets.rest";

export const useGetDataset = (): GetDataset => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Dataset>(getDatasetREST);

    const getDataset = (id: number): Promise<Dataset | undefined> => {
        return makeRequest(id);
    };

    return { dataset: data, getDataset, status, errorMessages };
};

export const useGetDatasets = (): GetDatasets => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Dataset[]>(getAllDatasets);

    const getDatasets = (): Promise<Dataset[] | undefined> => {
        return makeRequest();
    };

    return { datasets: data, getDatasets, status, errorMessages };
};
