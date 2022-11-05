/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { LogicalMetric } from "../../rest/dto/metric.interfaces";

export interface MetricsWizardProps {
    datasets: Dataset[];
    metric?: LogicalMetric;
    showCancel?: boolean;
    onChange?: (metricsWizardStep: MetricsWizardStep) => void;
    onCancel?: () => void;
    onFinish?: (metric: LogicalMetric) => void;
}

export enum MetricsWizardStep {
    METRIC_PROPERTIES,
    REVIEW_AND_SUBMIT,
}
