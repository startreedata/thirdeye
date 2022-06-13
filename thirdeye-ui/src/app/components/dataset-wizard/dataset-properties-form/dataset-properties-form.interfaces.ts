import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { Datasource } from "../../../rest/dto/datasource.interfaces";

export interface DatasetPropertiesFormProps {
    id: string;
    dataset?: Dataset;
    datasources: Datasource[];
    onSubmit?: (dataset: Dataset) => void;
}
