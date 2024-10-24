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
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    Grid,
    Link,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { MouseEvent, useCallback, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { easyAlertStyles } from "../../../pages/alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";
import { AlertTemplate as AlertTemplateType } from "../../../rest/dto/alert-template.interfaces";
import { TemplatePropertiesObject } from "../../../rest/dto/alert.interfaces";
import { getAlertTemplatesCreatePath } from "../../../utils/routes/routes.util";
import { AlertTemplatesInformationLinks } from "../../alert-wizard-v3/alert-templates-information-links/alert-templates-information-links";
import { InputSectionV2 } from "../../form-basics/input-section-v2/input-section-v2.component";
import { useAlertWizardV2Styles } from "../alert-wizard-v2.styles";
import { AlertTemplatePropertiesBuilder } from "./alert-template-properties-builder/alert-template-properties-builder-v2.component";
import { AlertTemplateProps } from "./alert-template.interfaces";
import {
    determinePropertyFieldConfiguration,
    hasRequiredPropertyValuesSet,
} from "./alert-template.utils";
import { PreviewChart } from "./preview-chart/preview-chart-v2.component";

function AlertTemplate({
    alert,
    onAlertPropertyChange,
    selectedAlertTemplate,
    setSelectedAlertTemplate,
    alertTemplateOptions,
    onChartDataLoadSuccess,
}: AlertTemplateProps): JSX.Element {
    const [alertTemplateProperties, setAlertTemplateProperties] =
        useState<TemplatePropertiesObject>(alert.templateProperties || {});

    const availableInputFields = useMemo(() => {
        if (selectedAlertTemplate) {
            return determinePropertyFieldConfiguration(selectedAlertTemplate);
        }

        return [];
    }, [selectedAlertTemplate]);

    const isRequiredPropertyValuesSet = useMemo(() => {
        return (
            !!selectedAlertTemplate &&
            hasRequiredPropertyValuesSet(
                availableInputFields,
                alertTemplateProperties
            )
        );
    }, [availableInputFields, alertTemplateProperties]);

    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();
    const easyAlertClasses = easyAlertStyles();

    const handlePropertyValueChange = (
        newChanges: TemplatePropertiesObject
    ): void => {
        setAlertTemplateProperties((original) => {
            const newTemplateProperties = {
                ...original,
                ...newChanges,
            };

            /**
             * If user entered empty a value for an optional field, assume
             * they want to delete the override
             */
            Object.keys(newChanges).forEach((templatePropertyKey) => {
                const propertyMetadata = availableInputFields.find(
                    (item) => item.name === templatePropertyKey
                );
                if (propertyMetadata && propertyMetadata.isOptional) {
                    if (newChanges[templatePropertyKey] === "") {
                        delete newTemplateProperties[templatePropertyKey];
                    }
                }
            });
            onAlertPropertyChange({
                templateProperties: newTemplateProperties,
            });

            return newTemplateProperties;
        });
    };

    const handleAlertTemplateChange = (
        selectedAlertTemplate: AlertTemplateType | null
    ): void => {
        if (!selectedAlertTemplate) {
            setSelectedAlertTemplate(null);
            onAlertPropertyChange({
                template: {},
            });

            return;
        }

        if (selectedAlertTemplate.id === -1) {
            return;
        }

        setSelectedAlertTemplate(selectedAlertTemplate);
        onAlertPropertyChange({
            template: {
                name: selectedAlertTemplate.name,
            },
        });
    };

    // Since this returns a JSX for render, wrapping this in a  useCallback
    // can help with ui performance and prevent unnecessary re-renders
    const renderAlertTemplateSelectOption = useCallback(
        (option: AlertTemplateType): JSX.Element => {
            if (option.id === -1) {
                return (
                    <Link
                        href={getAlertTemplatesCreatePath()}
                        target="_blank"
                        onClick={(e: MouseEvent<HTMLElement>) => {
                            // The link to create a new alert template
                            // should not be able to actually selected
                            e.preventDefault();
                            e.stopPropagation();
                            window.open(
                                getAlertTemplatesCreatePath(),
                                "_blank"
                            );
                        }}
                    >
                        {t("label.create-new-template")}
                    </Link>
                );
            }

            return (
                <li>
                    <Typography variant="h6">{option.name}</Typography>
                    <Typography variant="caption">
                        {option.description}
                    </Typography>
                </li>
            );
        },
        []
    );

    return (
        <Grid container>
            <Grid item xs={12}>
                <Box marginBottom={2}>
                    <Typography className={classes.header} variant="h6">
                        {t("label.detection-template")}
                    </Typography>
                    <Typography variant="body2">
                        {t("label.select-the-detection-type-for-your-data")}
                    </Typography>
                </Box>
            </Grid>
            <Grid item xs={4}>
                <InputSectionV2
                    description={t(
                        "message.select-a-template-to-monitor-an-detect-anomalies"
                    )}
                    inputComponent={
                        <>
                            <Autocomplete<AlertTemplateType>
                                fullWidth
                                getOptionLabel={(option) =>
                                    option.name as string
                                }
                                noOptionsText={t(
                                    "message.no-filter-options-available-entity",
                                    {
                                        entity: t("label.alert-template"),
                                    }
                                )}
                                options={alertTemplateOptions}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                            // Override class name so the size of input is smaller
                                            className:
                                                classes.autoCompleteInput,
                                        }}
                                        placeholder={t(
                                            "message.click-here-to-select-alert-template"
                                        )}
                                        variant="outlined"
                                    />
                                )}
                                renderOption={renderAlertTemplateSelectOption}
                                value={selectedAlertTemplate}
                                onChange={(_, selectedValue) => {
                                    handleAlertTemplateChange(selectedValue);
                                }}
                            />
                        </>
                    }
                    label={t("label.template")}
                />
            </Grid>
            <Grid item xs={4}>
                <Box alignItems="center" display="flex" height="100%">
                    <Button
                        className={easyAlertClasses.infoButton}
                        color="primary"
                        size="small"
                        variant="outlined"
                    >
                        <Box component="span" display="flex" mr={1}>
                            <Icon
                                fontSize={24}
                                icon="mdi:info-circle-outline"
                            />
                        </Box>
                        <Box component="span">
                            <AlertTemplatesInformationLinks />
                        </Box>
                    </Button>
                </Box>
            </Grid>

            {selectedAlertTemplate && (
                <AlertTemplatePropertiesBuilder
                    alertTemplateId={selectedAlertTemplate.id}
                    availableFields={availableInputFields}
                    templateProperties={alertTemplateProperties}
                    onPropertyValueChange={handlePropertyValueChange}
                />
            )}

            <Grid item xs={12}>
                <PreviewChart
                    alert={alert}
                    hideCallToActionPrompt={
                        !!selectedAlertTemplate && isRequiredPropertyValuesSet
                    }
                    onAlertPropertyChange={onAlertPropertyChange}
                    onChartDataLoadSuccess={onChartDataLoadSuccess}
                />
            </Grid>
        </Grid>
    );
}

export { AlertTemplate };
