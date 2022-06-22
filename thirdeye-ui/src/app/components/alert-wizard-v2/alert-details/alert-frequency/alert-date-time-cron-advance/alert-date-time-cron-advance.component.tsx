/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { FormHelperText, Grid, InputLabel, TextField } from "@material-ui/core";
import CronValidator from "cron-expression-validator";
import cronstrue from "cronstrue";
import { uniq } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CreateAlertConfigurationSection } from "../../../../../pages/alerts-create-page/alerts-create-page.interfaces";
import { ensureArrayOfStrings } from "../../../alert-template/alert-template.utils";
import { useAlertWizardV2Styles } from "../../../alert-wizard-v2.styles";
import { AlertDateTimeCronAdvanceProps } from "./alert-date-time-cron-advance.interfaces";

export const AlertDateTimeCronAdvance: FunctionComponent<
    AlertDateTimeCronAdvanceProps
> = ({ cron, onCronChange, onValidationChange }): JSX.Element => {
    const [currentCron, setCurrentCron] = useState<string>(cron);
    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();
    const isCronValid = CronValidator.isValidCronExpression(currentCron);

    // Ensure the parent always has an entry for cron
    useEffect(() => {
        onValidationChange(CreateAlertConfigurationSection.CRON, isCronValid);
    }, [currentCron]);

    const handleCronInputChange = (newCron: string): void => {
        setCurrentCron(newCron);

        // no guarantee the new cron has been put in state
        const isNewCronValid = CronValidator.isValidCronExpression(newCron);

        if (isNewCronValid) {
            onCronChange(newCron);
        }
    };

    return (
        <Grid container item xs={12}>
            <Grid item lg={2} md={4} sm={12} xs={12}>
                <InputLabel
                    shrink
                    className={classes.label}
                    data-testid="cron-input-label"
                    error={!isCronValid}
                >
                    {t("label.cron")}
                </InputLabel>
            </Grid>
            <Grid item lg={3} md={5} sm={12} xs={12}>
                <TextField
                    fullWidth
                    data-testid="cron-input"
                    error={!isCronValid}
                    value={currentCron}
                    variant="outlined"
                    onChange={(e) =>
                        handleCronInputChange(e.currentTarget.value)
                    }
                />

                {isCronValid && (
                    <FormHelperText className={classes.label}>
                        {cronstrue.toString(currentCron, {
                            verbose: true,
                        })}
                    </FormHelperText>
                )}

                {/* If there are errors, render them */}
                {!isCronValid &&
                    uniq(
                        ensureArrayOfStrings(
                            CronValidator.isValidCronExpression(currentCron, {
                                error: true,
                            }).errorMessage as string[]
                        )
                    ).map((item, idx) => {
                        return (
                            <FormHelperText
                                error
                                className={classes.label}
                                key={`${idx}`}
                            >
                                {item}
                            </FormHelperText>
                        );
                    })}
            </Grid>
        </Grid>
    );
};
