import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";

export interface DatasetListV1Props {
    datasets: UiDataset[] | null;
    onDelete?: (uiDatasets: UiDataset[]) => void;
}
