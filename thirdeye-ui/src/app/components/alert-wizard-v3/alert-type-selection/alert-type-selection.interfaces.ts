/*
 * Copyright 2023 StarTree Inc
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
import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface AlertTypeSelectionProps {
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
    onSelectionComplete: () => void;
    alertTemplates: AlertTemplate[];
}

export interface AlgorithmOption {
    title: string;
    description: string;
    alertTemplate: string;
    alertTemplateForMultidimension: string;
    alertTemplateForPercentile: string;
    exampleImage: string;
}

export interface AvailableAlgorithmOption {
    algorithmOption: AlgorithmOption;
    hasAlertTemplate: boolean;
    hasPercentile: boolean;
    hasMultidimension: boolean;
}
