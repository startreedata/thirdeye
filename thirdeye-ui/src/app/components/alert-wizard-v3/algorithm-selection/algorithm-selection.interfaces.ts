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
import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface AlgorithmSelectionProps {
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
    onSelectionComplete: (isDimensionExploration: boolean) => void;
    simpleOptions: AvailableAlgorithmOption[];
    advancedOptions: AvailableAlgorithmOption[];
}

export interface AlgorithmOptionInputFieldConfig {
    label: string;
    description: string;
    templatePropertyName: string;
    type: string;
    min?: number;
    max?: number;
}

export interface SliderAlgorithmOptionInputFieldConfig
    extends AlgorithmOptionInputFieldConfig {
    type: "slider";
    min: number;
    max: number;
}

export interface AlgorithmOption {
    title: string;
    description: string;
    alertTemplate: string;
    alertTemplateForMultidimension: string;
    alertTemplateForPercentile: string;
    inputFieldConfigs?: (
        | AlgorithmOptionInputFieldConfig
        | SliderAlgorithmOptionInputFieldConfig
    )[];
}

export interface AvailableAlgorithmOption {
    algorithmOption: AlgorithmOption;
    hasAlertTemplate: boolean;
    hasPercentile: boolean;
    hasMultidimension: boolean;
}
