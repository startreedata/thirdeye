import {
    Box,
    Grid,
    InputLabel,
    Link,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { MouseEvent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { AlertTemplate as AlertTemplateType } from "../../../rest/dto/alert-template.interfaces";
import { TemplatePropertiesObject } from "../../../rest/dto/alert.interfaces";
import { getAlertTemplatesCreatePath } from "../../../utils/routes/routes.util";
import { useAlertWizardV2Styles } from "../alert-wizard-v2.styles";
import { AlertTemplatePropertiesBuilder } from "./alert-template-properties-builder/alert-template-properties-builder.component";
import { AlertTemplateProps } from "./alert-template.interfaces";
import {
    findRequiredFields,
    hasRequiredPropertyValuesSet,
} from "./alert-template.utils";
import { PreviewChart } from "./preview-chart/preview-chart.component";
import { MessageDisplayState } from "./preview-chart/preview-chart.interfaces";

function AlertTemplate({
    alert,
    onAlertPropertyChange,
    selectedAlertTemplate,
    setSelectedAlertTemplate,
    alertTemplateOptions,
}: AlertTemplateProps): JSX.Element {
    const [alertTemplateProperties, setAlertTemplateProperties] =
        useState<TemplatePropertiesObject>(alert.templateProperties || {});

    const [isRequiredPropertyValuesSet, setIsRequiredPropertyValuesSet] =
        useState(false);

    const requiredFields = useMemo(() => {
        if (selectedAlertTemplate) {
            return findRequiredFields(selectedAlertTemplate);
        }

        return [];
    }, [selectedAlertTemplate]);

    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();

    useEffect(() => {
        const isValid =
            !!selectedAlertTemplate &&
            hasRequiredPropertyValuesSet(
                requiredFields,
                alertTemplateProperties,
                selectedAlertTemplate.defaultProperties || {}
            );

        setIsRequiredPropertyValuesSet(isValid);
    }, [selectedAlertTemplate, alertTemplateProperties]);

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
                if (
                    selectedAlertTemplate &&
                    selectedAlertTemplate.defaultProperties &&
                    selectedAlertTemplate.defaultProperties[
                        templatePropertyKey
                    ] !== undefined
                ) {
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
        selectedAlertTemplate: AlertTemplateType
    ): void => {
        if (!selectedAlertTemplate) {
            return;
        }
        setSelectedAlertTemplate(selectedAlertTemplate);
        onAlertPropertyChange({
            template: {
                name: selectedAlertTemplate.name,
            },
        });
    };

    const renderAlertTemplateSelectOption = (
        option: AlertTemplateType
    ): JSX.Element => {
        if (option.id === -1) {
            return (
                <Link
                    href={getAlertTemplatesCreatePath()}
                    target="_blank"
                    onClick={(e: MouseEvent<HTMLElement>) => {
                        // The link to create a new alert template
                        // should not able able to actually selected
                        e.preventDefault();
                        e.stopPropagation();
                        window.open(getAlertTemplatesCreatePath(), "_blank");
                    }}
                >
                    {t("label.create-new-template")}
                </Link>
            );
        }

        return <li>{option.name}</li>;
    };

    return (
        <PageContentsCardV1>
            <Grid container>
                <Grid item xs={12}>
                    <Box marginBottom={2}>
                        <Typography variant="h5">
                            {t("label.detection-template")}
                        </Typography>
                        <Typography variant="body2">
                            {t("message.select-template-to-preview-alert")}
                        </Typography>
                    </Box>
                </Grid>

                <Grid item xs={12}>
                    <Grid container>
                        <Grid item lg={2} md={4} sm={12} xs={12}>
                            <InputLabel
                                shrink
                                className={classes.label}
                                data-testid="template-type-input-label"
                            >
                                {t("label.template-type")}
                            </InputLabel>
                        </Grid>
                        <Grid item lg={3} md={5} sm={12} xs={12}>
                            <Autocomplete<AlertTemplateType>
                                disableClearable
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
                                        variant="outlined"
                                    />
                                )}
                                renderOption={renderAlertTemplateSelectOption}
                                value={selectedAlertTemplate}
                                onChange={(_, selectedValue) => {
                                    selectedValue &&
                                        selectedValue.id !== -1 &&
                                        handleAlertTemplateChange(
                                            selectedValue
                                        );
                                }}
                            />
                        </Grid>
                    </Grid>
                </Grid>

                {selectedAlertTemplate && (
                    <AlertTemplatePropertiesBuilder
                        alertTemplateId={selectedAlertTemplate.id}
                        defaultTemplateProperties={
                            selectedAlertTemplate.defaultProperties || {}
                        }
                        requiredFields={requiredFields}
                        templateProperties={alertTemplateProperties}
                        onPropertyValueChange={handlePropertyValueChange}
                    />
                )}

                <PreviewChart
                    alert={alert}
                    displayState={
                        selectedAlertTemplate
                            ? isRequiredPropertyValuesSet
                                ? MessageDisplayState.GOOD_TO_PREVIEW
                                : MessageDisplayState.FILL_TEMPLATE_PROPERTY_VALUES
                            : MessageDisplayState.SELECT_TEMPLATE
                    }
                />
            </Grid>
        </PageContentsCardV1>
    );
}

export { AlertTemplate };
