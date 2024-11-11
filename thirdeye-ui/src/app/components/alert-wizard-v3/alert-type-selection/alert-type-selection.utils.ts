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

import i18n from "i18next";
import AbsoluteScreenshot from "../../../../assets/images/alert-type-examples/absolute.png";
import ETSScreenshot from "../../../../assets/images/alert-type-examples/exponential_time_smoothing.png";
import MatrixProfileScreenshot from "../../../../assets/images/alert-type-examples/matrix_profile.png";
import MeanVarianceScreenshot from "../../../../assets/images/alert-type-examples/mean_variance.png";
import PercentageRuleScreenshot from "../../../../assets/images/alert-type-examples/percentage_change.png";
import ThresholdScreenshot from "../../../../assets/images/alert-type-examples/threshold.png";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import {
    AlgorithmOption,
    AvailableAlgorithmOption,
} from "./alert-type-selection.interfaces";

export const generateOptions = (): AlgorithmOption[] => {
    return [
        {
            title: i18n.t("label.threshold"),
            description: i18n.t("message.threshold-algorithm-description"),
            alertTemplate: "startree-threshold",
            alertTemplateForMultidimension: "startree-threshold-dx",
            alertTemplateForMultidimensionQuery: "startree-threshold-query-dx",
            alertTemplateForPercentile: "startree-threshold-percentile",
            exampleImage: ThresholdScreenshot,
        },
        {
            title: i18n.t("label.mean-variance-rule"),
            description: i18n.t(
                "message.mean-variance-rule-algorithm-description"
            ),
            alertTemplate: "startree-mean-variance",
            alertTemplateForMultidimension: "startree-mean-variance-dx",
            alertTemplateForMultidimensionQuery:
                "startree-mean-variance-query-dx",
            alertTemplateForPercentile: "startree-mean-variance-percentile",
            exampleImage: MeanVarianceScreenshot,
        },
        {
            title: i18n.t("label.percentage-rule"),
            description: i18n.t(
                "message.percentage-rule-algorithm-description"
            ),
            alertTemplate: "startree-percentage-rule",
            alertTemplateForMultidimension: "startree-percentage-rule-dx",
            alertTemplateForMultidimensionQuery:
                "startree-percentage-rule-query-dx",
            alertTemplateForPercentile: "startree-percentage-percentile",
            exampleImage: PercentageRuleScreenshot,
        },
        {
            title: i18n.t("label.absolute-change-rule"),
            description: i18n.t(
                "message.absolute-change-rule-algorithm-description"
            ),
            alertTemplate: "startree-absolute-rule",
            alertTemplateForMultidimension: "startree-absolute-rule-dx",
            alertTemplateForMultidimensionQuery:
                "startree-absolute-rule-query-dx",
            alertTemplateForPercentile: "startree-absolute-percentile",
            exampleImage: AbsoluteScreenshot,
        },
        {
            title: i18n.t("label.startree-ets"),
            description: i18n.t("message.startree-ets-algorithm-description"),
            alertTemplate: "startree-ets",
            alertTemplateForMultidimension: "startree-ets-dx",
            alertTemplateForMultidimensionQuery: "startree-ets-query-dx",
            alertTemplateForPercentile: "startree-ets-percentile",
            exampleImage: ETSScreenshot,
        },
        {
            title: i18n.t("label.matrix-profile"),
            description: i18n.t(
                "message.the-matrix-profile-method-is-a-direct-anomaly"
            ),
            alertTemplate: "startree-matrix-profile",
            alertTemplateForMultidimension: "startree-matrix-profile-dx",
            alertTemplateForMultidimensionQuery:
                "startree-matrix-profile-query-dx",
            alertTemplateForPercentile: "startree-matrix-profile-percentile",
            exampleImage: MatrixProfileScreenshot,
        },
    ];
};

export const filterOptionWithTemplateNames = (
    options: AvailableAlgorithmOption[],
    filterForMultiDimension: boolean
): AvailableAlgorithmOption[] => {
    return options.filter((c) =>
        filterForMultiDimension ? c.hasMultidimension : c.hasAlertTemplate
    );
};

export const generateAvailableAlgorithmOptions = (
    availableTemplateNames: string[]
): AvailableAlgorithmOption[] => {
    const availableOptions = generateOptions();

    return availableOptions.map((option) => {
        return {
            algorithmOption: option,
            hasAlertTemplate: availableTemplateNames.includes(
                option.alertTemplate
            ),
            hasPercentile: availableTemplateNames.includes(
                option.alertTemplateForPercentile
            ),
            hasMultidimension: availableTemplateNames.includes(
                option.alertTemplateForMultidimension
            ),
            hasMultidimensionQuery: availableTemplateNames.includes(
                option.alertTemplateForMultidimensionQuery!
            ),
            recommendationLabel: i18n.t("label.manual-configuration"),
        };
    });
};

export const generateAvailableAlgorithmOptionsForRecommendations = (
    alertRecommendations: { alert: EditableAlert }[],
    isMultiDimensional?: boolean
): AvailableAlgorithmOption[] => {
    const availableOptions = generateOptions();

    const availableAlgorithmOptions = alertRecommendations.map((rec, index) => {
        const algorithmOption = availableOptions.find(
            (option) =>
                option.alertTemplate === rec.alert.template?.name ||
                (isMultiDimensional &&
                    option.alertTemplateForMultidimension ===
                        rec.alert.template?.name)
        );

        if (!algorithmOption) {
            return null;
        }

        return {
            algorithmOption: {
                ...algorithmOption,
                title: `${algorithmOption.title} option ${index + 1}`,
            },
            hasAlertTemplate: true,
            hasPercentile: true,
            hasMultidimension: true,
            recommendationLabel: i18n.t("label.recommended-configuration"),
            recommendationId: `${rec.alert.template?.name}-${index}`,
        };
    });

    return availableAlgorithmOptions.filter(
        (option) => option !== null
    ) as AvailableAlgorithmOption[];
};
