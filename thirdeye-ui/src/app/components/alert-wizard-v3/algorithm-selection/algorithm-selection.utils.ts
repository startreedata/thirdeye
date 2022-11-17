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

import { AlgorithmOption } from "./algorithm-selection.interfaces";

export const generateOptions = (
    t: (id: string) => string
): [AlgorithmOption[], AlgorithmOption[]] => {
    return [
        // simple
        [
            {
                title: t("label.threshold"),
                description: t("message.threshold-algorithm-description"),
                alertTemplate: "startree-threshold",
                alertTemplateForMultidimension: "startree-threshold-dx",
                alertTemplateForPercentile: "startree-threshold-percentile",
            },
            {
                title: t("label.mean-variance-rule"),
                description: t(
                    "message.mean-variance-rule-algorithm-description"
                ),
                alertTemplate: "startree-mean-variance",
                alertTemplateForMultidimension: "startree-mean-variance-dx",
                alertTemplateForPercentile: "startree-mean-variance-percentile",
            },
            {
                title: t("label.percentage-rule"),
                description: t("message.percentage-rule-algorithm-description"),
                alertTemplate: "startree-percentage-rule",
                alertTemplateForMultidimension: "startree-percentage-rule-dx",
                alertTemplateForPercentile: "startree-percentage-percentile",
            },
            {
                title: t("label.absolute-change-rule"),
                description: t(
                    "message.absolute-change-rule-algorithm-description"
                ),
                alertTemplate: "startree-absolute-rule",
                alertTemplateForMultidimension: "startree-absolute-rule-dx",
                alertTemplateForPercentile: "startree-absolute-percentile",
            },
        ],
        // advanced
        [
            {
                title: t("label.holt-winters-rule"),
                description: t(
                    "message.holt-winters-rule-algorithm-description"
                ),
                alertTemplate: "startree-holt-winters",
                alertTemplateForMultidimension: "startree-holt-winters-dx",
                alertTemplateForPercentile: "startree-holt-winters-percentile",
            },
            {
                title: t("label.startree-ets"),
                description: t("message.startree-ets-algorithm-description"),
                alertTemplate: "startree-ets",
                alertTemplateForMultidimension: "startree-ets-dx",
                alertTemplateForPercentile: "startree-ets-percentile",
            },
        ],
    ];
};
