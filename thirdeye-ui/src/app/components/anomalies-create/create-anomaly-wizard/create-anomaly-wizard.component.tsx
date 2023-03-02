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

import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { EditedAnomaly } from "../../../pages/anomalies-create-page/anomalies-create-page.interfaces";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../platform/components";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { useGetEnumerationItems } from "../../../rest/enumeration-items/enumeration-items.actions";
import { generateDateRangeDaysFromNow } from "../../../utils/routes/routes.util";
import { WizardBottomBar } from "../../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { CreateAnomalyPropertiesForm } from "../create-anomaly-properties-form/create-anomaly-properties-form.component";
import {
    CreateAnomalyEditableFormFields,
    CreateAnomalyReadOnlyFormFields,
    CreateAnomalyWizardProps,
    HandleSetFields,
} from "./create-anomaly-wizard.interfaces";
import { getEnumerationItemsConfigFromAlert } from "./create-anomaly-wizard.utils";

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
        const {
            enumerationItems,
            getEnumerationItems,
            status: enumerationItemsStatus,
        } = useGetEnumerationItems();

        const [formFields, setFormFields] =
            useState<CreateAnomalyEditableFormFields>({
                alert: null,
                enumerationItem: null,
                dateRange: generateDateRangeDaysFromNow(3),
            });

        useEffect(() => {
            if (
                formFields.alert &&
                getEnumerationItemsConfigFromAlert(formFields.alert)
            ) {
                getEnumerationItems({
                    alertId: formFields.alert.id,
                });
            }
        }, [formFields.alert]);

        const readOnlyFormFields =
            useMemo<CreateAnomalyReadOnlyFormFields>(() => {
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

        const handleSetField: HandleSetFields = (fieldName, fieldValue) => {
            setFormFields((stateProp) => ({
                ...stateProp,
                [fieldName]: fieldValue,

                // Clear out enumeration items if alert is set
                ...(fieldName === "alert" &&
                    !!((fieldValue as Alert).id !== stateProp.alert?.id) && {
                        enumerationItem: null,
                    }),
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
                                    <CreateAnomalyPropertiesForm
                                        alerts={alerts}
                                        enumerationItemsForAlert={
                                            enumerationItems || []
                                        }
                                        enumerationItemsStatus={
                                            enumerationItemsStatus
                                        }
                                        formFields={formFields}
                                        handleSetField={handleSetField}
                                        readOnlyFormFields={readOnlyFormFields}
                                    />
                                    {/* <Divider />
                                    <pre>
                                        {JSON.stringify(
                                            formFields,
                                            undefined,
                                            4
                                        )}
                                    </pre> */}
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
