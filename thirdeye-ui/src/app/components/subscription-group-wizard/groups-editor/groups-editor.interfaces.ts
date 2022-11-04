import { FunctionComponent } from "react";
import {
    NotificationSpec,
    SpecType,
    SubscriptionGroup,
} from "../../../rest/dto/subscription-group.interfaces";

export interface GroupsEditorProps {
    subscriptionGroup: SubscriptionGroup;
    onSubscriptionGroupEmailsChange: (emails: string[]) => void;
    onSpecsChange: (specs: NotificationSpec[]) => void;
}

export interface SpecUIConfig {
    id: SpecType;
    internationalizationString: string;
    icon: string;
    /**
     * There is no simple way to type the expected props for the different
     * spec configurations
     */
    /* eslint-disable  @typescript-eslint/no-explicit-any */
    formComponent: FunctionComponent<any>;
    /* eslint-disable  @typescript-eslint/no-explicit-any */
    reviewComponent: FunctionComponent<any>;
    /* eslint-disable  @typescript-eslint/no-explicit-any */
    viewComponent: FunctionComponent<any>;
    validate: (spec: NotificationSpec) => boolean;
}
