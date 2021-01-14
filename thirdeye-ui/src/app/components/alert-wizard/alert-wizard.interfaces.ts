import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface AlertWizardProps {
    alert?: Alert;
    subscriptionGroups: SubscriptionGroup[];
    showCancel?: boolean;
    onChange?: (alertWizardStep: AlertWizardStep) => void;
    onCancel?: () => void;
    onFinish?: (alert: Alert) => void;
}

export enum AlertWizardStep {
    DETECTION_CONFIGURATION,
    SUBSCRIPTION_GROUPS,
    REVIEW_AND_SUBMIT,
}
