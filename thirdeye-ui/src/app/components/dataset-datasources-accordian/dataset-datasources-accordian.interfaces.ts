import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";

export interface DatasetDatasourcesAccordianProps {
    dataset: UiDataset | null;
    datasources: Datasource[];
    title: string;
    defaultExpanded?: boolean;
    onChange?: (datasources: Datasource[]) => void;
}
