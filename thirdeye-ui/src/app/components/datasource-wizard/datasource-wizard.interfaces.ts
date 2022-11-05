import { Datasource } from "../../rest/dto/datasource.interfaces";

export interface DatasourceWizardProps {
    isCreate?: boolean;
    datasource?: Datasource;
    onCancel?: () => void;
    onSubmit?: (datasource: Datasource, onboardDatasets: boolean) => void;
    submitBtnLabel: string;
}
