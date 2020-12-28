import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupWizardProps {
    subscriptionGroup?: SubscriptionGroup;
    showCancel?: boolean;
    getAlerts?: () => Promise<Alert[]>;
    onChange?: (
        subscriptionGroupWizardStep: SubscriptionGroupWizardStep
    ) => void;
    onCancel?: () => void;
    onFinish?: (subscriptionGroup: SubscriptionGroup) => void;
}

export enum SubscriptionGroupWizardStep {
    SUBSCRIPTION_GROUP_PROPERTIES,
    SUBSCRIBE_ALERTS,
    REVIEW_AND_SUBMIT,
}
