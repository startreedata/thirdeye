import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupPropertiesFormProps {
    id: string;
    subscriptionGroup?: SubscriptionGroup;
    onSubmit?: (subscriptionGroup: SubscriptionGroup) => void;
}
