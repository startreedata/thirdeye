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
import { yupResolver } from "@hookform/resolvers/yup";
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    Card,
    CardContent,
    FormHelperText,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { LocalThemeProviderV1 } from "../../../../platform/components";
import { lightV1 } from "../../../../platform/utils";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { SlackFormEntries, SlackProps } from "./slack.interfaces";

export const Slack: FunctionComponent<SlackProps> = ({
    configuration,
    onSpecChange,
    onDeleteClick,
}) => {
    const { t } = useTranslation();
    const { register, errors } = useForm<SlackFormEntries>({
        mode: "onChange",
        reValidateMode: "onChange",
        defaultValues: configuration.params,
        resolver: yupResolver(
            yup.object().shape({
                webhookUrl: yup
                    .string()
                    .trim()
                    .required(t("message.url-required")),
            })
        ),
    });

    const handleWebhookUrlChange = (newValue: string): void => {
        const copied = {
            ...configuration,
        };
        copied.params.webhookUrl = newValue;

        onSpecChange(copied);
    };

    return (
        <Card>
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
                <Grid container>
                    <InputSection
                        inputComponent={
                            <>
                                <TextField
                                    fullWidth
                                    data-testid="slack-input-container"
                                    error={Boolean(errors && errors.webhookUrl)}
                                    helperText={
                                        errors &&
                                        errors.webhookUrl &&
                                        errors.webhookUrl.message
                                    }
                                    inputRef={register}
                                    name="webhookUrl"
                                    type="string"
                                    variant="outlined"
                                    onChange={(e) =>
                                        handleWebhookUrlChange(
                                            e.currentTarget.value
                                        )
                                    }
                                />

                                <FormHelperText>
                                    e.g.
                                    https://company.slack.com/archives/XXXXXXXX
                                </FormHelperText>
                            </>
                        }
                        label={t("label.slack-url")}
                    />
                </Grid>
            </CardContent>
        </Card>
    );
};
