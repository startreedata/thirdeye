// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import {
    Alert,
    AlertEvaluation,
    EditableAlert,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface AlertWizardProps<NewOrExistingAlert> {
    alert: NewOrExistingAlert;
    createNewMode?: boolean;
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

export interface AlertWizardConfigurationNewProps {
    alertConfiguration: EditableAlert;
    error: boolean;
    hideTemplateSelector?: boolean;
    selectedTemplateId: string;
    helperText: string;
    onChange: (value: string) => void;
    onTemplateIdChange: (value: string) => void;
}

export enum AlertWizardStep {
    DETECTION_CONFIGURATION,
    SUBSCRIPTION_GROUPS,
    REVIEW_AND_SUBMIT,
}
