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
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    FormHelperText,
    Grid,
    Link,
    TextField,
} from "@material-ui/core";

import CronValidator from "cron-expression-validator";
import cronstrue from "cronstrue";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { easyAlertStyles } from "../../../pages/alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";
import { InputSectionV2 } from "../../form-basics/input-section-v2/input-section-v2.component";
import { CronAdvanceProps } from "./cron-advance.interfaces";
import { useCronAdvanceStyles } from "./cron-advance.styles";

export const CronAdvance: FunctionComponent<CronAdvanceProps> = ({
    cron,
    onCronChange,
    fullWidth = false,
}) => {
    const [currentCron, setCurrentCron] = useState<string>(cron);
    const { t } = useTranslation();
    const classes = useCronAdvanceStyles();
    const easyAlertClasses = easyAlertStyles();
    const isCronValid = CronValidator.isValidCronExpression(currentCron);

    const handleCronInputChange = (newCron: string): void => {
        setCurrentCron(newCron);
        onCronChange(newCron);
    };

    return (
        <Grid container>
            <Grid item xs={4}>
                <InputSectionV2
                    fullWidth={fullWidth}
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
                                <FormHelperText className={classes.label2}>
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
                    label={t("label.cron")}
                />
            </Grid>
            <Box alignItems="center" display="flex">
                <Link
                    href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
                    target="_blank"
                >
                    <Button
                        className={easyAlertClasses.infoButton}
                        color="primary"
                        size="small"
                        variant="outlined"
                    >
                        <Box component="span" display="flex" mr={1}>
                            <Icon
                                fontSize={24}
                                icon="mdi:info-circle-outline"
                            />
                        </Box>
                        <Box component="span">
                            {t("label.cron-documentation")}
                        </Box>
                    </Button>
                </Link>
            </Box>
        </Grid>
    );
};
