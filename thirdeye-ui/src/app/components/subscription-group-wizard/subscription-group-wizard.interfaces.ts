import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupWizardProps {
    subscriptionGroup: SubscriptionGroup;
    alerts: Alert[];
    showCancel?: boolean;
    onChange?: () => void;
    onCancel?: () => void;
    onFinish?: (subscriptionGroup: SubscriptionGroup) => void;
    submitBtnLabel: string;
}
