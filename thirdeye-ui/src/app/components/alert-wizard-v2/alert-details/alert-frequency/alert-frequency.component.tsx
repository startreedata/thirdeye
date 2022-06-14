import { Box, Grid, Typography } from "@material-ui/core";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Alert, EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { AlertDateTimeCronAdvance } from "./alert-date-time-cron-advance/alert-date-time-cron-advance.component";
import { AlertFrequencyProps } from "./alert-frequency.interfaces";

// 6AM Everyday
const DEFAULT_CRON = "0 6 * * *";

function AlertFrequency<NewOrExistingAlert extends EditableAlert | Alert>({
    alert,
    onAlertPropertyChange,
}: AlertFrequencyProps<NewOrExistingAlert>): JSX.Element {
    const [currentCron, setCurrentCron] = useState<string>(
        alert.cron ? alert.cron : DEFAULT_CRON
    );

    const { t } = useTranslation();

    const handleCronChange = (cron: string): void => {
        setCurrentCron(cron);
        onAlertPropertyChange({
            cron,
        });
    };

    return (
        <>
            <Grid item xs={12}>
                <Box marginBottom={2} marginTop={3}>
                    <Typography variant="h6">
                        {t("label.alert-frequency")}
                    </Typography>
                    <Typography variant="body2">
                        {t("message.how-often-pipeline-checks")}
                    </Typography>
                </Box>
            </Grid>

            <AlertDateTimeCronAdvance
                cron={currentCron}
                onCronChange={handleCronChange}
            />
        </>
    );
}

export { AlertFrequency };
