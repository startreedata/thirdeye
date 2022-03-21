import {
    Box,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
} from "@material-ui/core";
import { omit } from "lodash";
import React, { useEffect, useState } from "react";
import { JSONEditorV1 } from "../../platform/components";
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { createDefaultAlert } from "../../utils/alerts/alerts.util";
import { AlertWizardConfigurationNewProps } from "./alert-wizard.interfaces";

const DEFAULT_ALERT_TEMPLATE_ID = "DEFAULT";

function AlertWizardConfigurationNew({
    error,
    helperText,
    onChange,
    alertConfiguration,
    selectedTemplateId,
    onTemplateIdChange,
    hideTemplateSelector,
}: AlertWizardConfigurationNewProps): JSX.Element {
    const [currentWorkingConfiguration, setCurrentWorkingConfiguration] =
        useState(alertConfiguration);
    const [currentTemplate, setCurrentTemplate] = useState(selectedTemplateId);
    const { alertTemplates, getAlertTemplates } = useGetAlertTemplates();

    useEffect(() => {
        getAlertTemplates();
    }, []);

    useEffect(() => {
        if (!alertTemplates) {
            return;
        }
    }, [alertTemplates]);

    const handleChangeTemplateSelectionChange = (
        event: React.ChangeEvent<{ value: unknown }>
    ): void => {
        const selected = (event.target.value as string).toString();

        if (selected === DEFAULT_ALERT_TEMPLATE_ID) {
            setCurrentWorkingConfiguration(createDefaultAlert());
        } else if (alertTemplates) {
            const matchingAlertTemplate = alertTemplates.find(
                (candidate: AlertTemplate) => {
                    return candidate.id.toString() === selected;
                }
            );

            if (matchingAlertTemplate) {
                const cloned: EditableAlert = {
                    ...matchingAlertTemplate,
                    templateProperties: {},
                    name: "new-alert-name",
                } as EditableAlert;

                setCurrentWorkingConfiguration(omit(cloned, "id"));
                onChange(JSON.stringify(omit(cloned, "id")));
            }
        }
        onTemplateIdChange(selected);
        setCurrentTemplate(selected);
    };

    return (
        <div>
            {!hideTemplateSelector && (
                <Box mb={3}>
                    <FormControl fullWidth>
                        <InputLabel id="alert-templates-select-label">
                            Choose an alert template to start from
                        </InputLabel>
                        <Select
                            id="alert-templates-select"
                            labelId="alert-templates-select-label"
                            value={currentTemplate}
                            onChange={handleChangeTemplateSelectionChange}
                        >
                            <MenuItem value={DEFAULT_ALERT_TEMPLATE_ID}>
                                Default
                            </MenuItem>

                            {alertTemplates &&
                                alertTemplates.map((alertTemplate) => (
                                    <MenuItem
                                        key={alertTemplate.id}
                                        value={alertTemplate.id}
                                    >
                                        {alertTemplate.name}
                                    </MenuItem>
                                ))}
                        </Select>
                    </FormControl>
                </Box>
            )}

            <JSONEditorV1<EditableAlert>
                hideValidationSuccessIcon
                error={error}
                helperText={helperText}
                value={currentWorkingConfiguration}
                onChange={onChange}
            />
        </div>
    );
}

export { AlertWizardConfigurationNew, DEFAULT_ALERT_TEMPLATE_ID };
