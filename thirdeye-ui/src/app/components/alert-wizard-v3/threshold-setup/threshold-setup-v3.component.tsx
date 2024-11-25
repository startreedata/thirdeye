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
import { Box, Divider, Grid, Typography } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { TemplatePropertiesObject } from "../../../rest/dto/alert.interfaces";
import { isValidISO8601 } from "../../../utils/alerts/alerts.util";
import { AlertJsonEditorModal } from "../../alert-json-editor-modal/alert-json-editor-modal.component";
import { PreviewChartV3 } from "../../alert-wizard-v2/alert-template/preview-chart/preview-chart-v3.component";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { ParseMarkdown } from "../../parse-markdown/parse-markdown.component";
import { SpecificPropertiesRendererV3 } from "./specific-properties-renderer/specific-properties-renderer.component-v3";
import { ThresholdSetupProps } from "./threshold-setup.interfaces";
import { generateInputFieldConfigsForAlertTemplate } from "./threshold-setup.utils";

export const ThresholdSetupV3: FunctionComponent<ThresholdSetupProps> = ({
    onAlertPropertyChange,
    alert,
    alertTemplate,
    algorithmOptionConfig,
    alertEvaluation,
}) => {
    const { t } = useTranslation();

    const [localAlertTemplateProperties, setLocalAlertTemplateProperties] =
        useState<TemplatePropertiesObject>(alert.templateProperties);

    useEffect(() => {
        setLocalAlertTemplateProperties(
            alert.templateProperties as TemplatePropertiesObject
        );
    }, [alert]);

    const inputFieldConfigs = useMemo(() => {
        if (alertTemplate) {
            return generateInputFieldConfigsForAlertTemplate(alertTemplate);
        }

        return [];
    }, [alertTemplate, localAlertTemplateProperties]);

    /**
     * this is taken care of outside so that the source of truth object from the
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

    const unsetProperties = useMemo(() => {
        return inputFieldConfigs.filter((config) => {
            const value =
                localAlertTemplateProperties[config.templatePropertyName];
            if (value === 0) {
                return false;
            }

            return !value;
        });
    }, [inputFieldConfigs, localAlertTemplateProperties]);

    const invalidProperties = useMemo(() => {
        return inputFieldConfigs.filter((config) => {
            if (
                config.templatePropertyName !== "lookback" &&
                config.templatePropertyName !== "baselineOffset"
            ) {
                return false;
            }
            const value =
                localAlertTemplateProperties[config.templatePropertyName];
            if (!value) {
                return false;
            }

            return !isValidISO8601(value as string);
        });
    }, [inputFieldConfigs, localAlertTemplateProperties]);

    return (
        <PageContentsCardV1>
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Typography variant="h5">
                        {algorithmOptionConfig &&
                            t("label.entity-setup", {
                                entity: algorithmOptionConfig.algorithmOption
                                    .title,
                                multidimension:
                                    algorithmOptionConfig.algorithmOption
                                        .alertTemplateForMultidimension ===
                                    alert.template?.name
                                        ? `(${t("label.multidimension")})`
                                        : "",
                            })}
                    </Typography>
                </Grid>
                <Grid item>
                    <AlertJsonEditorModal
                        alert={alert}
                        onSubmitChanges={(newAlert, isTotalChange) => {
                            onAlertPropertyChange(newAlert, isTotalChange);

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

            <Box display="flex" flexDirection="row">
                {inputFieldConfigs.length > 0 &&
                    inputFieldConfigs.map((config) => {
                        return (
                            <InputSection
                                gridItemProps={{ xs: 6 }}
                                inputComponent={
                                    <>
                                        <SpecificPropertiesRendererV3
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
                                labelComponent={
                                    <Typography
                                        style={{
                                            fontWeight: "bold",
                                        }}
                                        variant="body2"
                                    >
                                        {config.label}
                                    </Typography>
                                }
                            />
                        );
                    })}
            </Box>

            <Grid item xs={12}>
                <Box marginBottom={2} marginTop={2} padding={1}>
                    <Divider />
                </Box>
            </Grid>

            {unsetProperties.length > 0 ? (
                <Alert severity="warning" variant="outlined">
                    {t("message.following-values-must-be-provided")} :{" "}
                    <Typography style={{ fontWeight: "bold" }} variant="body2">
                        {unsetProperties
                            .map((property) => property.label)
                            .join(", ")}
                    </Typography>
                </Alert>
            ) : null}

            {invalidProperties.length > 0 ? (
                <Alert severity="warning" variant="outlined">
                    {t("message.following-values-dont-follow-expected-format")}{" "}
                    :{" "}
                    <Typography style={{ fontWeight: "bold" }} variant="body2">
                        {invalidProperties
                            .map((property) => property.label)
                            .join(", ")}
                    </Typography>
                </Alert>
            ) : null}

            <PreviewChartV3
                hideCallToActionPrompt
                alert={alert}
                alertEvaluation={
                    unsetProperties.length === 0 &&
                    invalidProperties.length === 0
                        ? alertEvaluation
                        : undefined
                }
                disableReload={
                    unsetProperties.length > 0 || invalidProperties.length > 0
                }
                hasAutoReload={!!algorithmOptionConfig?.recommendationId}
                onAlertPropertyChange={onAlertPropertyChange}
            />
        </PageContentsCardV1>
    );
};
