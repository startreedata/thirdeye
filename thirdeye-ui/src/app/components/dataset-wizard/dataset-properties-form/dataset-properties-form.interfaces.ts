import { Dataset } from "../../../rest/dto/dataset.interfaces";

export interface DatasetPropertiesFormProps {
    id: string;
    dataset?: Dataset;
    onSubmit?: (dataset: Dataset) => void;
}
