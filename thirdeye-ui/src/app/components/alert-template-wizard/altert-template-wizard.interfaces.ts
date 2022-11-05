export interface AlertTemplateWizardProps<NewOrExistingTemplate> {
    alertTemplate: NewOrExistingTemplate;
    showCancel?: boolean;
    onCancel?: () => void;
    onFinish?: (alertTemplate: NewOrExistingTemplate) => void;
}

export enum AlertTemplateWizardStep {
    ALERT_TEMPLATE_CONFIGURATION,
    REVIEW_AND_SUBMIT,
}
