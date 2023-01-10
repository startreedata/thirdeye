/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { AvailableAlgorithmOption } from "../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.interfaces";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface AlertsCreateGuidedPageProps {
    alert: EditableAlert;
    alertTemplates: AlertTemplate[];
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
    onSubmit: (alert: EditableAlert, suggestedName: string) => void;
    isCreatingAlert: boolean;
    emails?: string[];
    setEmails?: (emails: string[]) => void;
    selectedSubscriptionGroups?: SubscriptionGroup[];
    onSubscriptionGroupChange?: (groups: SubscriptionGroup[]) => void;
    getAlertTemplates: () => void;
    newSubscriptionGroup: SubscriptionGroup;
    onNewSubscriptionGroupChange: (editedGroup: SubscriptionGroup) => void;
}

export interface AlertCreatedGuidedPageOutletContext {
    alert: EditableAlert;
    onAlertPropertyChange: (
        contents: Partial<EditableAlert>,
        isTotalChange?: boolean
    ) => void;
    selectedAlgorithmOption: AvailableAlgorithmOption;
    newSubscriptionGroup: SubscriptionGroup;
    onNewSubscriptionGroupChange: (editedGroup: SubscriptionGroup) => void;
    handleCreateAlertClick: (alert: EditableAlert) => void;
    isCreatingAlert: boolean;

    simpleOptions: AvailableAlgorithmOption[];
    advancedOptions: AvailableAlgorithmOption[];
    getAlertTemplates: () => void;
    alertTemplates: AlertTemplate[];

    selectedSubscriptionGroups: SubscriptionGroup[];
    handleSubscriptionGroupChange: (groups: SubscriptionGroup[]) => void;
}
