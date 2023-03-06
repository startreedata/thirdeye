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
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../platform/rest/actions.interfaces";
import { linkRendererV1 } from "../../../platform/utils";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import { generateNameForEnumerationItem } from "../../../utils/enumeration-items/enumeration-items.util";
import { getAlertsAlertViewPath } from "../../../utils/routes/routes.util";
import { createTimeRangeDuration } from "../../../utils/time-range/time-range.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeButton } from "../../time-range/time-range-button/time-range-button.component";
import { TimeRange } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import {
    CreateAnomalyFormKeys,
    CreateAnomalyReadOnlyFormFields,
} from "../create-anomaly-wizard/create-anomaly-wizard.interfaces";
import { getEnumerationItemsConfigFromAlert } from "../create-anomaly-wizard/create-anomaly-wizard.utils";
import { CreateAnomalyPropertiesFormProps } from "./create-anomaly-properties-form.interfaces";

export const CreateAnomalyPropertiesForm: FunctionComponent<CreateAnomalyPropertiesFormProps> =
    ({
        alerts,
        formFields,
        readOnlyFormFields,
        handleSetField,
        enumerationItemsForAlert,
        enumerationItemsStatus,
    }) => {
        const { t } = useTranslation();

        const showEnumerationItemsField = !!(
            formFields.alert &&
            getEnumerationItemsConfigFromAlert(formFields.alert)
        );

        const formLabels: Record<CreateAnomalyFormKeys, string> = {
            alert: t("label.alert"),
            dataSource: t("label.datasource"),
            dataset: t("label.dataset"),
            dateRange: t("label.date-range"),
            enumerationItem: t("label.dimension"),
            metric: t("label.metric"),
        };

        const timeRangeDuration = createTimeRangeDuration(
            TimeRange.CUSTOM,
            formFields.dateRange[0],
            formFields.dateRange[1]
        );

        return (
            <>
                <InputSection
                    inputComponent={
                        <Autocomplete
                            fullWidth
                            getOptionLabel={(option) => option.name}
                            options={alerts}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    InputProps={{
                                        ...params.InputProps,
                                    }}
                                    margin="dense"
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
                            value={formFields.alert}
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

                {(
                    Object.keys(
                        readOnlyFormFields
                    ) as (keyof CreateAnomalyReadOnlyFormFields)[]
                ).map((readOnlyKey: keyof CreateAnomalyReadOnlyFormFields) => (
                    <InputSection
                        inputComponent={
                            <TextField
                                fullWidth
                                required
                                InputProps={{
                                    margin: "none",
                                    readOnly: true,
                                }}
                                name={readOnlyKey}
                                title="These properties are derived from the selected alert"
                                type="string"
                                value={readOnlyFormFields[readOnlyKey] || ""}
                                variant="outlined"
                            />
                        }
                        key={readOnlyKey}
                        label={formLabels[readOnlyKey]}
                    />
                ))}

                <LoadingErrorStateSwitch
                    isError={enumerationItemsStatus === ActionStatus.Error}
                    isLoading={enumerationItemsStatus === ActionStatus.Working}
                    loadingState={
                        <InputSection
                            inputComponent={
                                <SkeletonV1 height={60} width="100%" />
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
                                    fullWidth
                                    getOptionLabel={(option) =>
                                        generateNameForEnumerationItem(option)
                                    }
                                    options={enumerationItemsForAlert}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            InputProps={{
                                                ...params.InputProps,
                                            }}
                                            placeholder={capitalize(
                                                t(
                                                    "message.click-here-to-select-entity",
                                                    {
                                                        entity: t(
                                                            "label.enumeration-item"
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

                <InputSection
                    fullWidth
                    inputComponent={
                        <TimeRangeButton
                            timeRangeDuration={timeRangeDuration}
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
            </>
        );
    };
