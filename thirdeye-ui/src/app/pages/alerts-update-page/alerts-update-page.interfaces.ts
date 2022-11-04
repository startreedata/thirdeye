import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export type AlertsUpdatePageParams = {
    id: string;
};

export interface AlertsEditPageProps {
    submitButtonLabel: string;
    startingAlertConfiguration: EditableAlert;
    pageTitle: string;
    onSubmit: (alert: EditableAlert) => void;
    selectedSubscriptionGroups: SubscriptionGroup[];
    onSubscriptionGroupChange: (newGroups: SubscriptionGroup[]) => void;
}
