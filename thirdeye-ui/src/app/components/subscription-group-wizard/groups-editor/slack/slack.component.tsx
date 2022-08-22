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
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    Card,
    CardContent,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { LocalThemeProviderV1 } from "../../../../platform/components";
import { lightV1 } from "../../../../platform/utils";
import { SlackProps } from "./slack.interfaces";

export const Slack: FunctionComponent<SlackProps> = ({
    configuration,
    onSpecChange,
    onDeleteClick,
}) => {
    const { t } = useTranslation();

    const handleWebhookUrlChange = (newValue: string): void => {
        const copied = {
            ...configuration,
        };
        copied.params.webhookUrl = newValue;

        onSpecChange(copied);
    };

    return (
        <Card elevation={2}>
            <CardContent>
                <Grid container justifyContent="space-between">
                    <Grid item>
                        <Typography variant="h6">
                            <Icon height={12} icon="logos:slack-icon" />{" "}
                            {t("label.slack")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Box textAlign="right">
                            <LocalThemeProviderV1
                                primary={lightV1.palette.error}
                            >
                                <Button
                                    color="primary"
                                    data-testid="slack-delete-btn"
                                    onClick={onDeleteClick}
                                >
                                    {t("label.delete")}
                                </Button>
                            </LocalThemeProviderV1>
                        </Box>
                    </Grid>
                </Grid>
            </CardContent>
            <CardContent>
                <label>{t("message.enter-webhook-url")}</label>
                <TextField
                    fullWidth
                    data-testid="slack-input-container"
                    placeholder="https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
                    value={configuration.params.webhookUrl}
                    variant="outlined"
                    onChange={(e) =>
                        handleWebhookUrlChange(e.currentTarget.value)
                    }
                />
            </CardContent>
        </Card>
    );
};
