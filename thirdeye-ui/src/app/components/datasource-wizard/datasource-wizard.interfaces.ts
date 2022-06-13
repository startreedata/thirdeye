import { Datasource } from "../../rest/dto/datasource.interfaces";

export interface DatasourceWizardProps {
    isCreate?: boolean;
    datasource?: Datasource;
    showCancel?: boolean;
    onCancel?: () => void;
    onFinish?: (datasource: Datasource, onboardDatasets: boolean) => void;
}

export enum DatasourceWizardStep {
    DATASOURCE_CONFIGURATION,
    REVIEW_AND_SUBMIT,
}
