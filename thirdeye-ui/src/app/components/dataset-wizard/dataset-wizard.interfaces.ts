import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";

export interface DatasetWizardProps {
    dataset?: Dataset;
    datasources: Datasource[];
    showCancel?: boolean;
    onCancel?: () => void;
    onChange?: (datasetWizardStep: DatasetWizardStep) => void;
    onFinish?: (dataset: Dataset) => void;
}

export enum DatasetWizardStep {
    DATASET_PROPERTIES,
    REVIEW_AND_SUBMIT,
}
