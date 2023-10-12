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
import { Grid, Typography } from "@material-ui/core";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import {
    JSONEditorV1,
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../platform/components";
import { WizardBottomBar } from "../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { AlertTemplateWizardProps } from "./altert-template-wizard.interfaces";

function AlertTemplateWizard<NewOrExistingTemplate>({
    startingAlertTemplate,
    onFinish,
}: AlertTemplateWizardProps<NewOrExistingTemplate>): JSX.Element {
    const { t } = useTranslation();

    const [modifiedAlertTemplate, setModifiedAlertTemplate] =
        useState<NewOrExistingTemplate>(startingAlertTemplate);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Grid container>
                            {/* Step label */}
                            <Grid item sm={12}>
                                <Typography variant="h5">
                                    {t("label.alert-template-configuration")}
                                </Typography>
                            </Grid>

                            {/* Spacer */}
                            <Grid item sm={12} />

                            {/* Datasource configuration editor */}
                            <Grid item sm={12}>
                                <JSONEditorV1<NewOrExistingTemplate>
                                    hideValidationSuccessIcon
                                    value={modifiedAlertTemplate}
                                    onChange={(value) => {
                                        try {
                                            setModifiedAlertTemplate(
                                                JSON.parse(value)
                                            );
                                        } catch {
                                            // do nothing if invalid JSON string
                                        }
                                    }}
                                />
                            </Grid>
                        </Grid>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <WizardBottomBar
                handleNextClick={() => {
                    onFinish && onFinish(modifiedAlertTemplate);
                }}
                nextButtonLabel={t("label.submit")}
            />
        </>
    );
}

export { AlertTemplateWizard };
