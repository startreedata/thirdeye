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
import { Box, Grid, Typography } from "@material-ui/core";
import React from "react";
import { useTranslation } from "react-i18next";
import { Alert, EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { CronEditor } from "../../../cron-editor-v1/cron-editor-v1.component";
import { AlertFrequencyProps } from "./alert-frequency.interfaces";

function AlertFrequency<NewOrExistingAlert extends EditableAlert | Alert>({
    alert,
    onAlertPropertyChange,
}: AlertFrequencyProps<NewOrExistingAlert>): JSX.Element {
    const { t } = useTranslation();

    const handleCronChange = (cron: string): void => {
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

            <CronEditor cron={alert.cron} handleUpdateCron={handleCronChange} />
        </>
    );
}

export { AlertFrequency };
