import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface AlertWizardProps {
    alert?: Alert;
    showCancel?: boolean;
    getAllSubscriptionGroups: () => Promise<SubscriptionGroup[]>;
    getAllAlerts: () => Promise<Alert[]>;
    onCancel?: () => void;
    onSubscriptionGroupWizardFinish: (
        sub: SubscriptionGroup
    ) => Promise<SubscriptionGroup>;
    onFinish?: (alert: Alert, subscriptionGroups: SubscriptionGroup[]) => void;
}

export enum AlertWizardStep {
    DETECTION_CONFIGURATION,
    SUBSCRIPTION_GROUPS,
    REVIEW_AND_SUBMIT,
}
