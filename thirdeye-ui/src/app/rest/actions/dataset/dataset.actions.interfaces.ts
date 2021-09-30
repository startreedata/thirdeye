import { Dataset } from "../../dto/dataset.interfaces";
import { ActionHook } from "../actions.interfaces";

export interface FetchDataset extends ActionHook {
    dataset: Dataset | null;
    fetchDataset: (id: number) => Promise<void>;
}

export interface FetchAllDataset extends ActionHook {
    datasets: Dataset[] | null;
    fetchAllDataset: () => Promise<void>;
}

export interface CreateDataset extends ActionHook {
    createDataset: (dataset: Dataset) => Promise<void>;
}

export interface CreateDatasets extends ActionHook {
    createDatasets: (datasets: Dataset[]) => Promise<void>;
}

export interface OnBoardDataset extends ActionHook {
    onBoardDataset: (
        datasetName: string,
        dataSourceName: string
    ) => Promise<void>;
}

export interface UpdateDataset extends ActionHook {
    updateDataset: (dataset: Dataset) => Promise<void>;
}

export interface UpdateDatasets extends ActionHook {
    updateDatasets: (datasets: Dataset[]) => Promise<void>;
}

export interface DeleteDataset extends ActionHook {
    deleteDataset: (id: number) => Promise<void>;
}
