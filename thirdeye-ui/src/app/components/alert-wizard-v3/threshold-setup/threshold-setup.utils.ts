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
import { Dispatch, SetStateAction } from "react";
import {
    EditableAlert,
    TemplatePropertiesObject,
} from "../../../rest/dto/alert.interfaces";
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import { DatasetInfo } from "../../../utils/datasources/datasources.util";

export function generateTemplateProperties(
    metric: string,
    dataset: Dataset,
    aggregationFunction: string
): TemplatePropertiesObject {
    const templateProperties: TemplatePropertiesObject = {
        dataSource: dataset.dataSource.name,
        dataset: dataset.name,
        aggregationColumn: metric,
        aggregationFunction: aggregationFunction,
    };

    if (dataset) {
        templateProperties.timeColumn = dataset.timeColumn.name;
        templateProperties.timeColumnFormat = dataset.timeColumn.format;
        templateProperties.timezone = dataset.timeColumn.timezone;
    }

    return templateProperties;
}

export function resetSelectedMetrics(
    datasetsInfo: DatasetInfo[],
    alertConfiguration: EditableAlert,
    setSelectedTable: Dispatch<SetStateAction<DatasetInfo | null>>,
    setSelectedMetric: Dispatch<SetStateAction<string | null>>,
    setSelectedAggregationFunction: Dispatch<SetStateAction<MetricAggFunction>>
): void {
    const newlySelectedDataset = datasetsInfo.find((candidate) => {
        return (
            candidate.dataset.name ===
                alertConfiguration.templateProperties?.dataset &&
            candidate.dataset.dataSource.name ===
                alertConfiguration.templateProperties?.dataSource
        );
    });

    setSelectedTable(newlySelectedDataset || null);

    if (newlySelectedDataset) {
        setSelectedMetric(
            (alertConfiguration.templateProperties
                ?.aggregationColumn as string) || null
        );
    } else {
        setSelectedMetric(null);
    }

    if (alertConfiguration.templateProperties?.aggregationFunction) {
        setSelectedAggregationFunction(
            alertConfiguration.templateProperties
                .aggregationFunction as MetricAggFunction
        );
    }
}
