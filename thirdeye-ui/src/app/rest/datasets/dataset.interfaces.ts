import { ActionHook } from "../actions.interfaces";
import { Dataset } from "../dto/dataset.interfaces";

export interface GetDataset extends ActionHook {
    dataset: Dataset | null;
    getDataset: (id: number) => Promise<Dataset | undefined>;
}

export interface GetDatasets extends ActionHook {
    datasets: Dataset[] | null;
    getDatasets: () => Promise<Dataset[] | undefined>;
}
