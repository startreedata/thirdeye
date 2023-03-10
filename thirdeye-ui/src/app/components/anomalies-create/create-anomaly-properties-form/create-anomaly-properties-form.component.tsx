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

import { TextField, Typography } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { capitalize } from "lodash";
import React, { FunctionComponent, ReactNode, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../platform/rest/actions.interfaces";
import { linkRendererV1 } from "../../../platform/utils";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import { generateNameForEnumerationItem } from "../../../utils/enumeration-items/enumeration-items.util";
import { getAlertsAlertViewPath } from "../../../utils/routes/routes.util";
import { createTimeRangeDuration } from "../../../utils/time-range/time-range.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeButton } from "../../time-range/time-range-button/time-range-button.component";
import { TimeRange } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { CreateAnomalyFormKeys } from "../create-anomaly-wizard/create-anomaly-wizard.interfaces";
import { useCreateAnomalyWizardStyles } from "../create-anomaly-wizard/create-anomaly-wizard.styles";
import { getEnumerationItemsConfigFromAlert } from "../create-anomaly-wizard/create-anomaly-wizard.utils";
import { CreateAnomalyPropertiesFormProps } from "./create-anomaly-properties-form.interfaces";

export const CreateAnomalyPropertiesForm: FunctionComponent<CreateAnomalyPropertiesFormProps> =
    ({
        alerts,
        formFields,
        handleSetField,
        enumerationItemsForAlert,
        enumerationItemsStatus,
        timezone,
    }) => {
        const { t } = useTranslation();
        const classes = useCreateAnomalyWizardStyles();

        const showEnumerationItemsField = !!(
            formFields.alert &&
            getEnumerationItemsConfigFromAlert(formFields.alert)
        );

        const formLabels: Record<CreateAnomalyFormKeys, string> = {
            alert: t("label.alert"),
            dateRange: t("label.date-range"),
            enumerationItem: t("label.dimension"),
        };

        const timeRangeDuration = createTimeRangeDuration(
            TimeRange.CUSTOM,
            formFields.dateRange[0],
            formFields.dateRange[1]
        );

        const renderAlertOption = useCallback(
            (option: Alert): ReactNode => {
                const {
                    dataSource,
                    dataset,
                    aggregationFunction,
                    aggregationColumn,
                } = option.templateProperties as {
                    dataSource: string;
                    dataset: string;
                    aggregationFunction: string;
                    aggregationColumn: string;
                };
                const metric = `${aggregationFunction}(${aggregationColumn})`;

                return (
                    <div>
                        <Typography variant="h6">{option.name}</Typography>
                        {[
                            [t("label.datasource"), dataSource],
                            [t("label.dataset"), dataset],
                            [t("label.metric"), metric],
                        ].map(([label, value]) => (
                            <Typography
                                color="textSecondary"
                                display="block"
                                key={label}
                                variant="caption"
                            >
                                <strong>{label}</strong>: {value}
                            </Typography>
                        ))}
                    </div>
                );
            },
            [alerts]
        );

        return (
            <>
                <InputSection
                    inputComponent={
                        <Autocomplete
                            disableClearable
                            getOptionLabel={(option) => option.name}
                            options={alerts}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    InputProps={{
                                        ...params.InputProps,
                                        // Override class name so the size of input is smaller
                                        className: classes.autoCompleteInput,
                                    }}
                                    margin="normal"
                                    placeholder={capitalize(
                                        t(
                                            "message.click-here-to-select-entity",
                                            {
                                                entity: t("label.alert"),
                                            }
                                        )
                                    )}
                                    size="small"
                                    variant="outlined"
                                />
                            )}
                            renderOption={renderAlertOption}
                            value={formFields.alert || undefined}
                            onChange={(_, selectedValue) => {
                                selectedValue &&
                                    handleSetField("alert", selectedValue);
                            }}
                        />
                    }
                    labelComponent={
                        <>
                            <Typography variant="body2">
                                {formLabels.alert}
                            </Typography>
                            {!!formFields.alert &&
                                linkRendererV1(
                                    t("label.view-entity", {
                                        entity: t("label.alert"),
                                    }),
                                    getAlertsAlertViewPath(formFields.alert.id),
                                    false,
                                    undefined,
                                    true,
                                    "_blank"
                                )}
                        </>
                    }
                />
                <LoadingErrorStateSwitch
                    isError={enumerationItemsStatus === ActionStatus.Error}
                    isLoading={enumerationItemsStatus === ActionStatus.Working}
                    loadingState={
                        <InputSection
                            inputComponent={
                                <SkeletonV1
                                    height={40}
                                    variant="rect"
                                    width="100%"
                                />
                            }
                            label={formLabels.enumerationItem}
                        />
                    }
                >
                    {!!(
                        showEnumerationItemsField &&
                        enumerationItemsForAlert &&
                        enumerationItemsForAlert.length > 0
                    ) && (
                        <InputSection
                            inputComponent={
                                <Autocomplete<EnumerationItem>
                                    getOptionLabel={(option) =>
                                        generateNameForEnumerationItem(option)
                                    }
                                    options={enumerationItemsForAlert}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            InputProps={{
                                                ...params.InputProps,
                                                // Override class name so the
                                                // size of input is smaller
                                                className:
                                                    classes.autoCompleteInput,
                                            }}
                                            placeholder={capitalize(
                                                t(
                                                    "message.click-here-to-select-entity",
                                                    {
                                                        entity: t(
                                                            "label.dimension"
                                                        ),
                                                    }
                                                )
                                            )}
                                            variant="outlined"
                                        />
                                    )}
                                    size="small"
                                    value={formFields.enumerationItem}
                                    onChange={(_, selectedValue) => {
                                        handleSetField(
                                            "enumerationItem",
                                            selectedValue
                                        );
                                    }}
                                />
                            }
                            label={formLabels.enumerationItem}
                        />
                    )}
                </LoadingErrorStateSwitch>

                {/* Only show the datetime picker when the timezone prop is passed, 
                to have the timezone shown be relevant to the selected alert  */}
                {!!timezone && (
                    <InputSection
                        helperLabel={t(
                            "message.select-the-start-and-end-date-time-range-for-the-anomalous-behavior"
                        )}
                        inputComponent={
                            <TimeRangeButton
                                hideQuickExtend
                                timeRangeDuration={timeRangeDuration}
                                timezone={timezone}
                                onChange={({ startTime, endTime }) =>
                                    handleSetField("dateRange", [
                                        startTime,
                                        endTime,
                                    ])
                                }
                            />
                        }
                        label={formLabels.dateRange}
                    />
                )}
            </>
        );
    };
