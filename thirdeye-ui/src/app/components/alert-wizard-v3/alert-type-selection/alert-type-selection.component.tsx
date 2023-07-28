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
import { Card, CardContent, Grid } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import React, { FunctionComponent, useMemo } from "react";
import { AlertTypeSection } from "./alert-type-section/alert-type-section.component";
import {
    AlertTypeSelectionProps,
    AlgorithmOption,
} from "./alert-type-selection.interfaces";
import { generateAvailableAlgorithmOptions } from "./alert-type-selection.utils";

export const AlertTypeSelection: FunctionComponent<AlertTypeSelectionProps> = ({
    onAlertPropertyChange,
    alertTemplates,
    isMultiDimensionAlert,
    selectedAlertTemplateName,
    recommendedAlertTemplate,
}) => {
    const handleAlgorithmClick = (algorithmOption: AlgorithmOption): void => {
        onAlertPropertyChange({
            template: {
                name: isMultiDimensionAlert
                    ? algorithmOption.alertTemplateForMultidimension
                    : algorithmOption.alertTemplate,
            },
        });
    };

    const options = useMemo(() => {
        return generateAvailableAlgorithmOptions(
            alertTemplates.map((a) => a.name)
        ).filter((option) =>
            isMultiDimensionAlert
                ? option.hasMultidimension
                : option.hasAlertTemplate
        );
    }, [alertTemplates]);

    const recommendedAlertTemplateFirst = useMemo(() => {
        const cloned = options.filter((c) => {
            return isMultiDimensionAlert
                ? c.algorithmOption.alertTemplateForMultidimension !==
                      recommendedAlertTemplate
                : c.algorithmOption.alertTemplate !== recommendedAlertTemplate;
        });

        const recommendedAlertTemplateOption = options.find((c) => {
            return isMultiDimensionAlert
                ? c.algorithmOption.alertTemplateForMultidimension ===
                      recommendedAlertTemplate
                : c.algorithmOption.alertTemplate === recommendedAlertTemplate;
        });

        if (recommendedAlertTemplateOption) {
            cloned.unshift(recommendedAlertTemplateOption);
        }

        return cloned;
    }, [alertTemplates, recommendedAlertTemplate]);

    return (
        <Grid container alignItems="stretch">
            {recommendedAlertTemplateFirst.map((option) => {
                const alertTemplateNameForOption = isMultiDimensionAlert
                    ? option.algorithmOption.alertTemplateForMultidimension
                    : option.algorithmOption.alertTemplate;
                const isSelected =
                    selectedAlertTemplateName === alertTemplateNameForOption;
                option.algorithmOption.alertTemplate;
                const isRecommended =
                    alertTemplateNameForOption === recommendedAlertTemplate;

                return (
                    <Grid
                        item
                        data-testId={`${alertTemplateNameForOption}-option-container`}
                        key={option.algorithmOption.title}
                        md={6}
                        sm={12}
                        xs={12}
                    >
                        <Card style={{ height: "100%" }} variant="outlined">
                            <CardContent
                                style={
                                    isSelected
                                        ? { height: "100%", padding: 0 }
                                        : { height: "100%" }
                                }
                            >
                                {isSelected ? (
                                    <Alert
                                        color="info"
                                        style={{ height: "100%" }}
                                        variant="standard"
                                    >
                                        <AlertTypeSection
                                            option={option}
                                            recommended={isRecommended}
                                            selected={isSelected}
                                            onClick={handleAlgorithmClick}
                                        />
                                    </Alert>
                                ) : (
                                    <AlertTypeSection
                                        option={option}
                                        recommended={isRecommended}
                                        selected={isSelected}
                                        onClick={handleAlgorithmClick}
                                    />
                                )}
                            </CardContent>
                        </Card>
                    </Grid>
                );
            })}
        </Grid>
    );
};
