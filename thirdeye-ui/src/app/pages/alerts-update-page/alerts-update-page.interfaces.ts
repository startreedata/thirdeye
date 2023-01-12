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
import { AlertTemplate as AlertTemplateType } from "../../rest/dto/alert-template.interfaces";
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

    newSubscriptionGroup: SubscriptionGroup;
    onNewSubscriptionGroupChange: (editedGroup: SubscriptionGroup) => void;
}

export interface AlertEditPageOutletContextProps {
    alert: EditableAlert;
    handleAlertPropertyChange: (
        contents: Partial<EditableAlert>,
        isTotalReplace?: boolean
    ) => void;
    selectedSubscriptionGroups: SubscriptionGroup[];
    handleSubscriptionGroupChange: (groups: SubscriptionGroup[]) => void;
    selectedAlertTemplate: AlertTemplateType;
    setSelectedAlertTemplate: (
        newAlertTemplate: AlertTemplateType | null
    ) => void;
    alertTemplateOptions: AlertTemplateType[];
    setShowBottomBar: (flag: boolean) => void;
    handleSubmitAlertClick: (alert: EditableAlert) => void;
    refreshAlertTemplates: () => void;

    newSubscriptionGroup: SubscriptionGroup;
    onNewSubscriptionGroupChange: (editedGroup: SubscriptionGroup) => void;
}
