///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

export interface NewAlertTemplate {
    name: string;
    description: string;
    cron?: string;
    nodes?: AlertTemplateNode[];

    rca?: {
        datasource: string;
        dataset: string;
        metric: string;
    };

    metadata?: {
        datasource: {
            name: string;
        };
        dataset: {
            name: string;
        };
        metric: {
            name: string;
        };
    };
}

export interface AlertTemplate extends NewAlertTemplate {
    id: number;
}

export interface AlertTemplateNode {
    name: string;
    type: string;
    params: { [index: string]: unknown };
    inputs?: AlertTemplateNodeInput[];
    outputs?: AlertTemplateNodeOutput[];
}

export interface AlertTemplateNodeInput {
    targetProperty: string;
    sourcePlanNode: string;
    sourceProperty: string;
}

export interface AlertTemplateNodeOutput {
    outputKey: string;
    outputName: string;
}
