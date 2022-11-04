import { Dataset } from "../../rest/dto/dataset.interfaces";

export interface DatasetWizardProps {
    dataset: Dataset;
    onCancel?: () => void;
    onSubmit?: (dataset: Dataset) => void;
    submitBtnLabel: string;
}
