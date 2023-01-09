/*
 * Copyright 2022 StarTree Inc
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
import CronValidator from "cron-expression-validator";
import {
    determinePropertyFieldConfiguration,
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
        const availableFields = determinePropertyFieldConfiguration(
            selectedAlertTemplate
        );
        onValidationChange(
            CreateAlertConfigurationSection.TEMPLATE_PROPERTIES,
            hasRequiredPropertyValuesSet(
                availableFields,
                alertConfig.templateProperties || {}
            )
        );
    } else {
        onValidationChange(
            CreateAlertConfigurationSection.TEMPLATE_PROPERTIES,
            false
        );
    }
}
