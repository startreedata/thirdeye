import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";

export interface AlertTemplateWizardProps {
    alertTemplate?: AlertTemplate;
    showCancel?: boolean;
    onCancel?: () => void;
    onFinish?: (alertTemplate: AlertTemplate) => void;
}

export enum AlertTemplateWizardStep {
    ALERT_TEMPLATE_CONFIGURATION,
    REVIEW_AND_SUBMIT,
}
