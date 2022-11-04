import {
    FormHelperText,
    InputLabel,
    Link,
    TextField,
    Typography,
} from "@material-ui/core";
import CronValidator from "cron-expression-validator";
import cronstrue from "cronstrue";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { InputSection } from "../../../../form-basics/input-section/input-section.component";
import { useAlertWizardV2Styles } from "../../../alert-wizard-v2.styles";
import { AlertDateTimeCronAdvanceProps } from "./alert-date-time-cron-advance.interfaces";

export const AlertDateTimeCronAdvance: FunctionComponent<AlertDateTimeCronAdvanceProps> =
    ({ cron, onCronChange }): JSX.Element => {
        const [currentCron, setCurrentCron] = useState<string>(cron);
        const { t } = useTranslation();
        const classes = useAlertWizardV2Styles();
        const isCronValid = CronValidator.isValidCronExpression(currentCron);

        const handleCronInputChange = (newCron: string): void => {
            setCurrentCron(newCron);
            onCronChange(newCron);
        };

        return (
            <InputSection
                inputComponent={
                    <>
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
                        {!isCronValid && (
                            <FormHelperText
                                error
                                className={classes.label}
                                data-testid="error-message-container"
                            >
                                {t("message.invalid-cron-input-1")}
                                <Link
                                    href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
                                    target="_blank"
                                >
                                    {t("label.cron-documentation")}
                                </Link>
                                {t("message.invalid-cron-input-2")}
                            </FormHelperText>
                        )}
                    </>
                }
                labelComponent={
                    <>
                        <InputLabel
                            shrink
                            className={classes.label}
                            data-testid="cron-input-label"
                            error={!isCronValid}
                        >
                            {t("label.cron")}
                        </InputLabel>
                        <Typography variant="caption">
                            <Link
                                href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
                                target="_blank"
                            >
                                {t("label.documentation")}
                            </Link>
                        </Typography>
                    </>
                }
            />
        );
    };
