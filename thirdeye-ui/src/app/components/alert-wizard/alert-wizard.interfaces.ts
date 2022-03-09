import {
    Alert,
    AlertEvaluation,
    EditableAlert,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface AlertWizardProps<NewOrExistingAlert> {
    alert: NewOrExistingAlert;
    showCancel?: boolean;
    getAllSubscriptionGroups: () => Promise<SubscriptionGroup[]>;
    getAllAlerts: () => Promise<Alert[]>;
    getAlertEvaluation: (alert: NewOrExistingAlert) => Promise<AlertEvaluation>;
    onCancel?: () => void;
    onSubscriptionGroupWizardFinish: (
        sub: SubscriptionGroup
    ) => Promise<SubscriptionGroup>;
    onFinish?: (
        alert: EditableAlert,
        subscriptionGroups: SubscriptionGroup[],
        omittedSubscriptionGroups?: SubscriptionGroup[]
    ) => void;
}

export enum AlertWizardStep {
    DETECTION_CONFIGURATION,
    SUBSCRIPTION_GROUPS,
    REVIEW_AND_SUBMIT,
}
