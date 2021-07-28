import { Dataset } from "./dataset.interfaces";

export interface UiDataset {
    id: number;
    name: string;
    datasources: UiDatasetDatasource[];
    datasourceCount: string;
    dataset: Dataset | null;
}

export interface UiDatasetDatasource {
    id: number;
    name: string;
}
