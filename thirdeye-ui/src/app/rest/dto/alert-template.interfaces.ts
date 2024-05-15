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

import { PropertyConfigValueTypes } from "./alert.interfaces";

export enum MetadataPropertyStep {
    DATA = "DATA",
    PREPROCESS = "PREPROCESS",
    DETECTION = "DETECTION",
    FILTER = "FILTER",
    POSTPROCESS = "POSTPROCESS",
    RCA = "RCA",
    OTHER = "OTHER",
}

export interface MetadataProperty {
    name: string;
    description?: string;
    defaultValue?: PropertyConfigValueTypes;
    defaultIsNull: boolean;
    multiselect: boolean;
    options?: string[];

    /**
     * See spec https://json-schema.org/understanding-json-schema/reference/type.html
     *
     * See https://github.com/startreedata/thirdeye/blob/master/thirdeye-spi/src
     * /main/java/ai/startree/thirdeye/spi/template/TemplatePropertyMetadata.java#L143
     * for the possible types with the backend
     */
    jsonType?:
        | "STRING"
        | "NUMBER"
        | "INTEGER"
        | "OBJECT"
        | "ARRAY"
        | "BOOLEAN"
        | "NULL";

    /**
     * Should indicate what part of the anomaly detection pipeline the property
     * affects
     *
     * See introduction PR https://github.com/startreedata/thirdeye/pull/974
     */
    step?: MetadataPropertyStep;
    /**
     * A free string subStep. Used to group properties belonging to the same
     * step in smaller groups.
     *
     * See introduction PR https://github.com/startreedata/thirdeye/pull/974
     */
    subStep?: string;
}

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

    defaultProperties?: { [index: string]: string };
    properties?: MetadataProperty[];
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
