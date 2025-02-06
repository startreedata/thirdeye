/*
 * Copyright 2025 StarTree Inc
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
import { Box, Typography } from "@material-ui/core";
import React, { useMemo } from "react";
import { InputSection } from "../../../../../components/form-basics/input-section/input-section.component";
import { SpecificPropertiesRenderer } from "../../../../../components/alert-wizard-v3/threshold-setup/specific-properties-renderer/specific-properties-renderer.component";
import { ParseMarkdown } from "../../../../../components/parse-markdown/parse-markdown.component";
import { useCreateAlertStore } from "../../../hooks/state";
import { EditableAlert } from "../../../../../rest/dto/alert.interfaces";
// utils
import { generateInputFieldConfigsForAlertTemplate } from "../../../../../components/alert-wizard-v3/threshold-setup/threshold-setup.utils";
import { isValidISO8601 } from "../../../../../utils/alerts/alerts.util";
import { Alert } from "@material-ui/lab";
import { useTranslation } from "react-i18next";

export const RequiredProperties = (): JSX.Element => {
    const { t } = useTranslation();
    const {
        alertTemplates,
        workingAlert,
        setWorkingAlert,
        setIsEvaluationDataStale,
    } = useCreateAlertStore();
    const inputFieldConfigs = useMemo(() => {
        const selectedTemplate = alertTemplates?.find(
            (alertTemplateCandidate) => {
                return (
                    alertTemplateCandidate.name ===
                    (workingAlert as EditableAlert).template?.name
                );
            }
        );
        if (selectedTemplate) {
            return generateInputFieldConfigsForAlertTemplate(selectedTemplate);
        }

        return [];
    }, [(workingAlert as EditableAlert).template?.name]);

    const handleExtraPropertyChange = (
        propertyName: string,
        newValue: string
    ): void => {
        const newTemplateProperties = {
            templateProperties: {
                ...(workingAlert as EditableAlert).templateProperties,
                [propertyName]: newValue,
            },
        };
        setWorkingAlert({
            ...workingAlert,
            templateProperties: newTemplateProperties.templateProperties,
        });
        setIsEvaluationDataStale(true);
    };
    const unsetProperties = useMemo(() => {
        return inputFieldConfigs.filter((config) => {
            const value =
                workingAlert.templateProperties![config.templatePropertyName];
            if (value === 0) {
                return false;
            }

            return !value;
        });
    }, [inputFieldConfigs, workingAlert.templateProperties]);

    const invalidProperties = useMemo(() => {
        return inputFieldConfigs.filter((config) => {
            if (
                config.templatePropertyName !== "lookback" &&
                config.templatePropertyName !== "baselineOffset"
            ) {
                return false;
            }
            const value =
                workingAlert.templateProperties![config.templatePropertyName];
            if (!value) {
                return false;
            }

            return !isValidISO8601(value as string);
        });
    }, [inputFieldConfigs, workingAlert.templateProperties]);

    return (
        <Box display="flex" flexDirection="column">
            <Box
                data-testId="required-properties"
                display="flex"
                flexDirection="row"
            >
                {inputFieldConfigs.length > 0 &&
                    inputFieldConfigs.map((config) => {
                        return (
                            <InputSection
                                gridItemProps={{ xs: 6 }}
                                inputComponent={
                                    <>
                                        <SpecificPropertiesRenderer
                                            inputFieldConfig={config}
                                            selectedTemplateProperties={
                                                (workingAlert as EditableAlert)
                                                    .templateProperties
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
                                        style={{ fontWeight: "bold" }}
                                        variant="body2"
                                    >
                                        {config.label}
                                        <span style={{ color: "red" }}>*</span>
                                    </Typography>
                                }
                            />
                        );
                    })}
            </Box>
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
        </Box>
    );
};
