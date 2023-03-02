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

import { Grid, TextField, Typography } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { EditedAnomaly } from "../../../pages/anomalies-create-page/anomalies-create-page.interfaces";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../platform/components";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { generateDateRangeDaysFromNow } from "../../../utils/routes/routes.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { WizardBottomBar } from "../../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { CreateAnomalyWizardProps } from "./create-anomaly-wizard.interfaces";

export const CreateAnomalyWizard: FunctionComponent<CreateAnomalyWizardProps> =
    ({
        alerts,

        submitBtnLabel,
        cancelBtnLabel,
        onSubmit,
        onCancel,
        initialAnomalyData,
    }) => {
        const { t } = useTranslation();

        const [editedAnomaly, setEditedAnomaly] =
            useState<EditedAnomaly>(initialAnomalyData);

        interface FormFields {
            alert: Alert | null;
            enumerationItem: number | null;
            dateRange: [number, number];
        }

        const [formFields, setFormFields] = useState<FormFields>({
            alert: null,
            enumerationItem: null,
            dateRange: generateDateRangeDaysFromNow(3),
        });

        const readOnlyFormFields = useMemo(() => {
            if (!formFields.alert) {
                return {
                    dataSource: null,
                    dataset: null,
                    metric: null,
                };
            }
            const {
                dataSource,
                dataset,
                aggregationColumn: metric,
            } = formFields.alert.templateProperties as {
                dataSource: string;
                dataset: string;
                aggregationColumn: string;
            };

            return {
                dataSource,
                dataset,
                metric,
            };
        }, [formFields.alert]);

        const handleCancelClick = (): void => {
            onCancel?.();
        };
        const handleSubmitClick = (): void => {
            onSubmit?.(editedAnomaly);
        };

        const handleSetField = <T extends keyof typeof formFields>(
            fieldName: T,
            fieldValue: FormFields[T]
        ): void => {
            setFormFields((stateProp) => ({
                ...stateProp,
                [fieldName]: fieldValue,
            }));
        };

        const isAnomalyValid = false;

        return (
            <>
                <PageContentsGridV1 fullHeight>
                    <Grid item xs={12}>
                        <PageContentsCardV1 fullHeight>
                            <Grid container alignItems="stretch">
                                <Grid item xs={12}>
                                    <Typography variant="h5">
                                        {t("label.setup-entity", {
                                            entity: t("label.anomaly"),
                                        })}
                                    </Typography>
                                    <Typography
                                        color="secondary"
                                        variant="subtitle1"
                                    >
                                        Configure details for the anomaly
                                        datetime range and the parent alert
                                    </Typography>
                                </Grid>

                                <Grid item xs={12}>
                                    <InputSection
                                        inputComponent={
                                            <Autocomplete
                                                fullWidth
                                                getOptionLabel={(option) =>
                                                    option.name
                                                }
                                                options={alerts}
                                                renderInput={(params) => (
                                                    <TextField
                                                        {...params}
                                                        InputProps={{
                                                            ...params.InputProps,
                                                        }}
                                                        placeholder={t(
                                                            "message.click-here-to-select-entity",
                                                            {
                                                                entity: t(
                                                                    "label.alert"
                                                                ),
                                                            }
                                                        )}
                                                        variant="outlined"
                                                    />
                                                )}
                                                size="small"
                                                value={formFields.alert}
                                                onChange={(
                                                    _,
                                                    selectedValue
                                                ) => {
                                                    handleSetField(
                                                        "alert",
                                                        selectedValue
                                                    );
                                                }}
                                            />
                                        }
                                        label={t("label.alert")}
                                    />

                                    {Object.entries(readOnlyFormFields).map(
                                        ([readOnlyKey, readOnlyValue]) => (
                                            <InputSection
                                                inputComponent={
                                                    <TextField
                                                        disabled
                                                        fullWidth
                                                        required
                                                        name={readOnlyKey}
                                                        type="string"
                                                        value={readOnlyValue}
                                                        variant="outlined"
                                                    />
                                                }
                                                key={readOnlyKey}
                                                label={readOnlyKey}
                                            />
                                        )
                                    )}
                                </Grid>
                            </Grid>
                        </PageContentsCardV1>
                    </Grid>
                </PageContentsGridV1>
                <WizardBottomBar
                    backButtonLabel={cancelBtnLabel}
                    handleBackClick={handleCancelClick}
                    handleNextClick={handleSubmitClick}
                    nextButtonIsDisabled={!isAnomalyValid}
                    nextButtonLabel={submitBtnLabel}
                />
            </>
        );
    };
