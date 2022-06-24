import CronValidator from "cron-expression-validator";
import {
    findRequiredFields,
    hasRequiredPropertyValuesSet,
} from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { CreateAlertConfigurationSection } from "../alerts-create-page.interfaces";

export function validateConfiguration(
    alertConfig: EditableAlert,
    selectedAlertTemplate: AlertTemplate,
    onValidationChange: (
        key: CreateAlertConfigurationSection,
        isValid: boolean
    ) => void
): void {
    // name validation
    if (alertConfig.name === undefined || alertConfig.name === "") {
        onValidationChange(CreateAlertConfigurationSection.NAME, false);
    } else {
        onValidationChange(CreateAlertConfigurationSection.NAME, true);
    }

    // cron validation
    if (alertConfig.cron === undefined || alertConfig.cron === "") {
        onValidationChange(CreateAlertConfigurationSection.CRON, false);
    } else {
        // If valid string, validate against expression checker
        onValidationChange(
            CreateAlertConfigurationSection.CRON,
            CronValidator.isValidCronExpression(alertConfig.cron)
        );
    }

    // alert template validation
    if (alertConfig.template === undefined || !alertConfig.template.name) {
        onValidationChange(
            CreateAlertConfigurationSection.TEMPLATE_PROPERTIES,
            false
        );
    } else if (selectedAlertTemplate) {
        const requiredFields = findRequiredFields(selectedAlertTemplate);
        onValidationChange(
            CreateAlertConfigurationSection.TEMPLATE_PROPERTIES,
            hasRequiredPropertyValuesSet(
                requiredFields,
                alertConfig.templateProperties || {},
                selectedAlertTemplate.defaultProperties || {}
            )
        );
    } else {
        onValidationChange(
            CreateAlertConfigurationSection.TEMPLATE_PROPERTIES,
            false
        );
    }
}
