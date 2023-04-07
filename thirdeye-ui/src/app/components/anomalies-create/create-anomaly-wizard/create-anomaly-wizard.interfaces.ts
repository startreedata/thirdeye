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

import { Alert } from "../../../rest/dto/alert.interfaces";
import { EditableAnomaly } from "../../../rest/dto/anomaly.interfaces";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";

export interface CreateAnomalyWizardProps {
    alerts: Alert[];
    submitBtnLabel: string;
    cancelBtnLabel: string;
    onSubmit: (anomaly: EditableAnomaly) => void;
    onCancel: () => void;
}

export interface CreateAnomalyEditableFormFields {
    alert: Alert | null;
    enumerationItem: EnumerationItem | null;
    dateRange: [number, number];
}

export interface SelectedAlertDetails {
    dataSource: string | null;
    dataset: string | null;
    metric: string | null;
}

export type HandleSetFields = <T extends keyof CreateAnomalyEditableFormFields>(
    fieldName: T,
    fieldValue: CreateAnomalyEditableFormFields[T]
) => void;
