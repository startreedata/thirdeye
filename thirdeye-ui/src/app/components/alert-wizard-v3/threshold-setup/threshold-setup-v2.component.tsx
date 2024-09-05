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
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { borderCardStyles } from "../../../pages/alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";
import { TemplatePropertiesObject } from "../../../rest/dto/alert.interfaces";
import { PreviewChart } from "../../alert-wizard-v2/alert-template/preview-chart/preview-chart.component";
import { InputSectionV2 } from "../../form-basics/input-section-v2/input-section-v2.component";
import { ParseMarkdown } from "../../parse-markdown/parse-markdown.component";
import { SpecificPropertiesRenderer } from "./specific-properties-renderer/specific-properties-renderer-v2.component";
import { ThresholdSetupProps } from "./threshold-setup.interfaces";
import { generateInputFieldConfigsForAlertTemplate } from "./threshold-setup.utils";

export const ThresholdSetup: FunctionComponent<ThresholdSetupProps> = ({
    onAlertPropertyChange,
    alert,
    alertTemplate,
    children,
}) => {
    const classes = borderCardStyles();

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

    return (
        <Box className={classes.card} marginTop={2}>
            <Grid container>
                {inputFieldConfigs.length > 0 &&
                    inputFieldConfigs.map((config, index) => {
                        return (
                            <Grid item key={index} xs={8}>
                                <InputSectionV2
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
                                    key={config.label}
                                    label={config.label}
                                />
                            </Grid>
                        );
                    })}
            </Grid>
            {inputFieldConfigs.length > 0 && (
                <Grid item xs={12}>
                    <Box marginBottom={2} marginTop={2} padding={1}>
                        <Divider />
                    </Box>
                </Grid>
            )}
            <PreviewChart
                hideCallToActionPrompt
                alert={alert}
                showTimeRange={false}
                onAlertPropertyChange={onAlertPropertyChange}
            >
                {children}
            </PreviewChart>
        </Box>
    );
};
