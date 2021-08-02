import { UiDataset } from "../../../rest/dto/ui-dataset.interfaces";

export interface DatasetCardProps {
    uiDataset: UiDataset | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiDataset: UiDataset) => void;
    onEdit?: (id: number) => void;
}
