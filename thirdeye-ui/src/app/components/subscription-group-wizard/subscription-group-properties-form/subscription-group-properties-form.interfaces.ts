import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupPropertiesFormProps {
    subscriptionGroup: SubscriptionGroup;
    onChange: (modifiedSubscriptionGroup: Partial<SubscriptionGroup>) => void;
}
