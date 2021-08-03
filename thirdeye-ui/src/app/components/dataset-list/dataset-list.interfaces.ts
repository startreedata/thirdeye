import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";

export interface DatasetListProps {
    hideSearchBar?: boolean;
    datasets: UiDataset[] | null;
    onDelete?: (uidataset: UiDataset) => void;
}
