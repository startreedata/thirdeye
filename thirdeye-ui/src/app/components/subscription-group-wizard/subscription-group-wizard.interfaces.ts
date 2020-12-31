import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupWizardProps {
    subscriptionGroup?: SubscriptionGroup;
    alerts: Alert[];
    showCancel?: boolean;
    onChange?: (
        subscriptionGroupWizardStep: SubscriptionGroupWizardStep
    ) => void;
    onCancel?: () => void;
    onFinish?: (subscriptionGroup: SubscriptionGroup) => void;
}

export enum SubscriptionGroupWizardStep {
    SUBSCRIPTION_GROUP_PROPERTIES,
    REVIEW_AND_SUBMIT,
}
