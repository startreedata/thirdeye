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
import { Box, Divider, Grid, TextField, Typography } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../platform/components";
import { TemplatePropertiesObject } from "../../../rest/dto/alert.interfaces";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import { STAR_COLUMN } from "../../../utils/datasources/datasources.util";
import { AlertJsonEditorModal } from "../../alert-json-editor-modal/alert-json-editor-modal.component";
import { PreviewChart } from "../../alert-wizard-v2/alert-template/preview-chart/preview-chart.component";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2/alert-wizard-v2.styles";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { ParseMarkdown } from "../../parse-markdown/parse-markdown.component";
import { NavigateAlertCreationFlowsDropdown } from "../navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { SpecificPropertiesRenderer } from "./specific-properties-renderer/specific-properties-renderer.component";
import { ThresholdSetupProps } from "./threshold-setup.interfaces";
import { generateInputFieldConfigsForAlertTemplate } from "./threshold-setup.utils";

export const ThresholdSetup: FunctionComponent<ThresholdSetupProps> = ({
    onAlertPropertyChange,
    alert,
    alertTemplate,
    algorithmOptionConfig,
}) => {
    const classes = useAlertWizardV2Styles();
    const { t } = useTranslation();

    const [localAlertTemplateProperties, setLocalAlertTemplateProperties] =
        useState<TemplatePropertiesObject>(alert.templateProperties);

    const [selectedAggregationFunction, setSelectedAggregationFunction] =
        useState<MetricAggFunction>(MetricAggFunction.SUM);

    const inputFieldConfigs = useMemo(() => {
        if (alertTemplate) {
            return generateInputFieldConfigsForAlertTemplate(alertTemplate);
        }

        return [];
    }, [alertTemplate, localAlertTemplateProperties]);

    const selectedMetric = useMemo(() => {
        return alert.templateProperties.aggregationColumn;
    }, [alert]);

    const handleAggregationFunctionSelect = (
        aggregationFunction: MetricAggFunction
    ): void => {
        setSelectedAggregationFunction(
            aggregationFunction as MetricAggFunction
        );
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                aggregationFunction: aggregationFunction,
            },
        });
    };

    /**
     * this is taken care of outseide so that the source of truth object from the
     * alerts config is used
     */
    const handleExtraPropertyChange = (
        propertyName: string,
        newValue: string
    ): void => {
        const newTemplateProperties = {
            templateProperties: {
                ...alert.templateProperties,
                [propertyName]: newValue,
            },
        };
        setLocalAlertTemplateProperties(
            newTemplateProperties.templateProperties as TemplatePropertiesObject
        );
        onAlertPropertyChange(newTemplateProperties);
    };

    return (
        <PageContentsGridV1>
            <Grid item xs={12}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item>
                        <Typography variant="h5">
                            {t("label.alert-setup")}
                        </Typography>
                        <Typography variant="body1">
                            {t("message.alert-setup-description")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <NavigateAlertCreationFlowsDropdown />
                    </Grid>
                </Grid>
            </Grid>
            <Grid item xs={12}>
                <PageContentsCardV1>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="space-between"
                    >
                        <Grid item>
                            <Typography variant="h5">
                                {algorithmOptionConfig &&
                                    t("label.entity-setup", {
                                        entity: algorithmOptionConfig
                                            .algorithmOption.title,
                                        multidimension:
                                            algorithmOptionConfig
                                                .algorithmOption
                                                .alertTemplateForMultidimension ===
                                            alert.template?.name
                                                ? `(${t(
                                                      "label.multidimension"
                                                  )})`
                                                : "",
                                    })}
                            </Typography>
                            <Typography variant="body2">
                                {t("message.threshold-setup-description")}
                            </Typography>
                        </Grid>
                        <Grid item>
                            <AlertJsonEditorModal
                                alert={alert}
                                onSubmitChanges={(newAlert, isTotalChange) => {
                                    onAlertPropertyChange(
                                        newAlert,
                                        isTotalChange
                                    );

                                    setLocalAlertTemplateProperties({
                                        ...newAlert.templateProperties,
                                    });
                                }}
                            />
                        </Grid>
                    </Grid>

                    {inputFieldConfigs.length > 0 && (
                        <Grid item xs={12}>
                            <Box padding={2} />
                        </Grid>
                    )}

                    <InputSection
                        inputComponent={
                            <Autocomplete
                                disableClearable
                                fullWidth
                                // Disable selection if selected column is *
                                disabled={selectedMetric === STAR_COLUMN}
                                options={
                                    selectedMetric === STAR_COLUMN
                                        ? [MetricAggFunction.COUNT]
                                        : [
                                              MetricAggFunction.SUM,
                                              MetricAggFunction.AVG,
                                              MetricAggFunction.COUNT,
                                              MetricAggFunction.MIN,
                                              MetricAggFunction.MAX,
                                          ]
                                }
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                            /**
                                             * Override class name so
                                             * the size of input is smaller
                                             */
                                            className:
                                                classes.autoCompleteInput,
                                        }}
                                        placeholder={t(
                                            "message.select-aggregation-function"
                                        )}
                                        variant="outlined"
                                    />
                                )}
                                value={selectedAggregationFunction}
                                onChange={(_, aggregationFunction) => {
                                    aggregationFunction &&
                                        handleAggregationFunctionSelect(
                                            aggregationFunction
                                        );
                                }}
                            />
                        }
                        label={`${t("label.aggregation-function")}`}
                    />

                    {inputFieldConfigs.length > 0 && (
                        <Grid item xs={12}>
                            <Box marginBottom={1} padding={2}>
                                <Divider />
                            </Box>
                        </Grid>
                    )}
                    {inputFieldConfigs.length > 0 &&
                        inputFieldConfigs.map((config) => {
                            return (
                                <InputSection
                                    inputComponent={
                                        <>
                                            <SpecificPropertiesRenderer
                                                inputFieldConfig={config}
                                                selectedTemplateProperties={
                                                    localAlertTemplateProperties
                                                }
                                                onAlertPropertyChange={
                                                    handleExtraPropertyChange
                                                }
                                            />
                                            {!!config.description && (
                                                <Typography variant="caption">
                                                    <ParseMarkdown>
                                                        {config.description}
                                                    </ParseMarkdown>
                                                </Typography>
                                            )}
                                        </>
                                    }
                                    key={config.templatePropertyName}
                                    label={config.label}
                                />
                            );
                        })}

                    <Grid item xs={12}>
                        <Box marginBottom={2} marginTop={2} padding={1}>
                            <Divider />
                        </Box>
                    </Grid>

                    <PreviewChart
                        hideCallToActionPrompt
                        alert={alert}
                        onAlertPropertyChange={onAlertPropertyChange}
                    />
                </PageContentsCardV1>
            </Grid>
        </PageContentsGridV1>
    );
};
