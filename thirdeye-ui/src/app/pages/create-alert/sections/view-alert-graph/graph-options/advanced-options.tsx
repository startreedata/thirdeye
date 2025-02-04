/*
 * Copyright 2024 StarTree Inc
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
// external
import React, { useMemo, useState } from "react";
import { Button } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import { cloneDeep } from "lodash";

// app components
import { useTranslation } from "react-i18next";
import { AdditonalFiltersDrawer } from "../../../../../components/additional-filters-drawer/additional-filters-drawer.component";
import { ReactComponent as FilterListRoundedIcon } from "../../../../../platform/assets/images/filter-icon.svg";

// state
import { useCreateAlertStore } from "../../../hooks/state";

// styles
import { graphOptionsStyles } from "./styles";

// types
import {
    EditableAlert,
    TemplatePropertiesObject,
} from "../../../../../rest/dto/alert.interfaces";
import { AnomalyDetectionOptions } from "../../../../../rest/dto/metric.interfaces";

// utils
import {
    determinePropertyFieldConfiguration,
    setUpFieldInputRenderConfig,
} from "../../../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { groupPropertyByStepAndSubStep } from "../../../../../utils/alerts/alerts.util";

export const AdvancedOptions = (): JSX.Element => {
    const { t } = useTranslation();
    const componentStyles = graphOptionsStyles();
    const {
        alertTemplates,
        anomalyDetectionType,
        selectedDetectionAlgorithm,
        workingAlert,
        setWorkingAlert,
        setIsEvaluationDataStale,
    } = useCreateAlertStore();
    const [showAdvancedOptions, setShowAdvancedOptions] = useState(false);

    const availableConfigurations = useMemo(() => {
        const isMultiDimensionAlert =
            anomalyDetectionType === AnomalyDetectionOptions.COMPOSITE;
        const alertTemplateToFind = isMultiDimensionAlert
            ? selectedDetectionAlgorithm?.algorithmOption
                  .alertTemplateForMultidimension
            : selectedDetectionAlgorithm?.algorithmOption.alertTemplate;
        const alertTemplateForEvaluate = alertTemplates?.find(
            (alertTemplateCandidate) => {
                return alertTemplateCandidate.name === alertTemplateToFind;
            }
        );

        if (!alertTemplateForEvaluate) {
            return undefined;
        }

        // return getAvailableFilterOptions(alertTemplateForEvaluate, t);
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const [_requiredKeys, optionalKeys] = setUpFieldInputRenderConfig(
            selectedDetectionAlgorithm
                ? determinePropertyFieldConfiguration(alertTemplateForEvaluate)
                : [],
            workingAlert.templateProperties!
        );

        return groupPropertyByStepAndSubStep(optionalKeys);
    }, [alertTemplates, anomalyDetectionType, selectedDetectionAlgorithm]);

    // const availableConfigurations = useMemo(() => {
    //     if (!alertTemplateForEvaluate) {
    //         return undefined;
    //     }

    //     // eslint-disable-next-line @typescript-eslint/no-unused-vars
    //     const [_requiredKeys, optionalKeys] = setUpFieldInputRenderConfig(
    //         selectedAlertTemplate
    //             ? determinePropertyFieldConfiguration(selectedAlertTemplate)
    //             : [],
    //         alert.templateProperties
    //     );

    //     return groupPropertyByStepAndSubStep(optionalKeys);

    //     // return getAvailableFilterOptions(alertTemplateForEvaluate, t);
    // }, [alertTemplateForEvaluate, selectedAlertTemplate]);

    const handleApplyAdvancedOptions = (
        fieldData: TemplatePropertiesObject
    ): void => {
        const clonedAlert = cloneDeep(workingAlert) as Partial<EditableAlert>;
        clonedAlert.templateProperties = {
            ...clonedAlert.templateProperties,
            ...fieldData,
        };
        setWorkingAlert(clonedAlert);
        setShowAdvancedOptions(false);
        setIsEvaluationDataStale(true);
    };

    return (
        <>
            <Button
                className={componentStyles.infoButton}
                color="primary"
                startIcon={<FilterListRoundedIcon />}
                variant="outlined"
                onClick={() => {
                    setShowAdvancedOptions(true);
                }}
            >
                {t("label.add-advanced-options")}
            </Button>
            <AdditonalFiltersDrawer
                availableConfigurations={availableConfigurations ?? {}}
                defaultValues={
                    (workingAlert as Partial<EditableAlert>).templateProperties!
                }
                emptyMessage={
                    !selectedDetectionAlgorithm ? (
                        <Alert severity="info" style={{ marginTop: "40px" }}>
                            {t(
                                "message.please-select-a-detection-algorithm-first"
                            )}
                        </Alert>
                    ) : null
                }
                isOpen={showAdvancedOptions}
                onApply={handleApplyAdvancedOptions}
                onClose={() => {
                    setShowAdvancedOptions(false);
                }}
            />
        </>
    );
};
