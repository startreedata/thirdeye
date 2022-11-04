import {
    Box,
    FormControl,
    FormControlLabel,
    Grid,
    Radio,
    RadioGroup,
    Typography,
} from "@material-ui/core";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useDialogProviderV1 } from "../../../../platform/components";
import { DialogType } from "../../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { Alert, EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { AlertDateTimeCronAdvance } from "./alert-date-time-cron-advance/alert-date-time-cron-advance.component";
import { AlertDateTimeCronSimple } from "./alert-date-time-cron-simple/alert-date-time-cron-simple.component";
import { isSimpleConvertible } from "./alert-date-time-cron-simple/alert-date-time-cron-simple.utils";
import { AlertFrequencyProps } from "./alert-frequency.interfaces";

enum CronMode {
    SIMPLE,
    ADVANCED,
}

function AlertFrequency<NewOrExistingAlert extends EditableAlert | Alert>({
    alert,
    onAlertPropertyChange,
}: AlertFrequencyProps<NewOrExistingAlert>): JSX.Element {
    const { showDialog } = useDialogProviderV1();
    const [currentCron, setCurrentCron] = useState<string>(alert.cron);

    const { t } = useTranslation();

    const [cronConfigTab, setCronConfigTab] = useState<CronMode>(
        currentCron
            ? isSimpleConvertible(currentCron)
                ? CronMode.SIMPLE
                : CronMode.ADVANCED
            : CronMode.ADVANCED
    );

    const handleCronChange = (cron: string): void => {
        setCurrentCron(cron);
        onAlertPropertyChange({
            cron,
        });
    };

    const handleCronModeChange = (mode: CronMode): void => {
        // If switching to simple mode, check if the cron is transferable
        if (mode === CronMode.SIMPLE && !isSimpleConvertible(currentCron)) {
            showDialog({
                type: DialogType.ALERT,
                contents: t("message.change-cron-warning"),
                okButtonText: t("label.ok"),
                cancelButtonText: t("label.cancel"),
                onOk: () => setCronConfigTab(mode),
            });
        } else {
            setCronConfigTab(mode);
        }
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

            <InputSection
                inputComponent={
                    <FormControl component="fieldset">
                        <RadioGroup
                            row
                            aria-label="cron-radio-buttons"
                            name="cron-radio-buttons"
                            value={cronConfigTab}
                            onChange={(e) =>
                                handleCronModeChange(
                                    Number(
                                        e.target.value
                                    ) as unknown as CronMode
                                )
                            }
                        >
                            <FormControlLabel
                                control={<Radio />}
                                label={t("label.day-time")}
                                value={CronMode.SIMPLE}
                            />
                            <FormControlLabel
                                control={<Radio />}
                                label={t("label.cron")}
                                value={CronMode.ADVANCED}
                            />
                        </RadioGroup>
                    </FormControl>
                }
                labelComponent={
                    <Box alignItems="center" display="flex" paddingTop={1}>
                        <Typography variant="body2">
                            {t("label.date-type")}
                        </Typography>
                    </Box>
                }
            />

            {cronConfigTab === CronMode.SIMPLE ? (
                <AlertDateTimeCronSimple
                    value={currentCron}
                    onChange={handleCronChange}
                />
            ) : (
                <AlertDateTimeCronAdvance
                    cron={currentCron}
                    onCronChange={handleCronChange}
                />
            )}
        </>
    );
}

export { AlertFrequency };
