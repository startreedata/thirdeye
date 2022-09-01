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
import {
    Box,
    FormControl,
    FormControlLabel,
    Grid,
    Radio,
    RadioGroup,
    Typography,
} from "@material-ui/core";
import HelpOutlineIcon from "@material-ui/icons/HelpOutline";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { TooltipV1 } from "../../../../platform/components";
import { Alert, EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { CronEditor } from "../../../cron-editor/cron-editor.component";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { AlertDateTimeCronAdvance } from "./alert-date-time-cron-advance/alert-date-time-cron-advance.component";
import { AlertFrequencyProps } from "./alert-frequency.interfaces";

function AlertFrequency<NewOrExistingAlert extends EditableAlert | Alert>({
    alert,
    onAlertPropertyChange,
}: AlertFrequencyProps<NewOrExistingAlert>): JSX.Element {
    const [currentCron, setCurrentCron] = useState<string>(alert.cron);
    const [dayTimeCron, setDayTimeCron] = useState<string>(alert.cron);

    const { t } = useTranslation();

    const [cronConfigTab, setCronConfigTab] = useState<string>(
        t("label.day-time")
    );

    useEffect(() => {
        if (cronConfigTab === t("label.day-time")) {
            onAlertPropertyChange({ cron: dayTimeCron });
        } else {
            onAlertPropertyChange({ cron: currentCron });
        }
    }, [cronConfigTab, currentCron, dayTimeCron]);

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
                            onChange={(e) => setCronConfigTab(e.target.value)}
                        >
                            <FormControlLabel
                                control={<Radio />}
                                label={t("label.day-time")}
                                value={t("label.day-time")}
                            />
                            <FormControlLabel
                                control={<Radio />}
                                label={t("label.cron")}
                                value={t("label.cron")}
                            />
                        </RadioGroup>
                    </FormControl>
                }
                labelComponent={
                    <Box alignItems="center" display="flex" paddingTop={1}>
                        <Typography variant="body2">
                            {t("label.date-type")}&nbsp;
                        </Typography>
                        <TooltipV1
                            title={t("message.data-type-helper") as string}
                        >
                            <HelpOutlineIcon
                                color="secondary"
                                fontSize="small"
                            />
                        </TooltipV1>
                    </Box>
                }
            />

            {cronConfigTab === t("label.day-time") ? (
                <CronEditor value={dayTimeCron} onChange={setDayTimeCron} />
            ) : (
                <AlertDateTimeCronAdvance
                    cron={currentCron}
                    onCronChange={setCurrentCron}
                />
            )}
        </>
    );
}

export { AlertFrequency };
