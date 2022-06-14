import { Grid, InputLabel, TextField, Typography } from "@material-ui/core";
import cronValidator from "cron-validate";
import cronstrue from "cronstrue";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAlertWizardV2Styles } from "../../../alert-wizard-v2.styles";
import { AlertDateTimeCronAdvanceProps } from "./alert-date-time-cron-advance.interfaces";

export const AlertDateTimeCronAdvance: FunctionComponent<
    AlertDateTimeCronAdvanceProps
> = ({ cron, onCronChange }): JSX.Element => {
    const [currentCron, setCurrentCron] = useState<string>(cron);
    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();
    const cronIsValidateResults = cronValidator(currentCron);

    const handleCronInputChange = (newCron: string): void => {
        setCurrentCron(newCron);

        // no guarantee the new cron has been put in state
        if (cronValidator(newCron).isValid()) {
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
                    error={!cronIsValidateResults.isValid()}
                >
                    {t("label.cron")}
                </InputLabel>
            </Grid>
            <Grid item lg={3} md={5} sm={12} xs={12}>
                <TextField
                    fullWidth
                    data-testid="cron-input"
                    error={!cronIsValidateResults.isValid()}
                    value={currentCron}
                    variant="outlined"
                    onChange={(e) =>
                        handleCronInputChange(e.currentTarget.value)
                    }
                />

                {cronIsValidateResults.isValid() && (
                    <Typography variant="caption">
                        {cronstrue.toString(currentCron, {
                            verbose: true,
                        })}
                    </Typography>
                )}

                {/* If there are errors, render them */}
                {!cronIsValidateResults.isValid() &&
                    cronIsValidateResults.getError().map((item, idx) => {
                        return <div key={`${idx}`}>{item}</div>;
                    })}
            </Grid>
        </Grid>
    );
};
