import { Datasource } from "../../rest/dto/datasource.interfaces";

export interface DatasourceWizardProps {
    datasource?: Datasource;
    showCancel?: boolean;
    onCancel?: () => void;
    onFinish?: (datasource: Datasource) => void;
}

export enum DatasourceWizardStep {
    DATASOURCE_CONFIGURATION,
    REVIEW_AND_SUBMIT,
}
