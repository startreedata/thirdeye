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

import { ActionStatus } from "../../../platform/rest/actions.interfaces";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import {
    CreateAnomalyEditableFormFields,
    HandleSetFields,
    SelectedAlertDetails,
} from "../create-anomaly-wizard/create-anomaly-wizard.interfaces";

export interface CreateAnomalyPropertiesFormProps {
    alerts: Alert[];
    selectedAlertDetails: SelectedAlertDetails | null;
    enumerationItemsForAlert: EnumerationItem[];
    formFields: CreateAnomalyEditableFormFields;
    handleSetField: HandleSetFields;
    enumerationItemsStatus: ActionStatus;
    timezone: string | undefined;
}
