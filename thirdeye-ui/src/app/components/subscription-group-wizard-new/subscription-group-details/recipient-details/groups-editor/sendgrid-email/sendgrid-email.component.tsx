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
import { yupResolver } from "@hookform/resolvers/yup";
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
import { cloneDeep } from "lodash";
import React, { FunctionComponent } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { LocalThemeProviderV1 } from "../../../../../../platform/components";
import { lightV1 } from "../../../../../../platform/utils";
import { EmailListInput } from "../../../../../form-basics/email-list-input/email-list-input.component";
import { InputSection } from "../../../../../form-basics/input-section/input-section.component";
import {
    SendgridEmailFormEntries,
    SendgridEmailProps,
} from "./sendgrid-email.interfaces";

export const SendgridEmail: FunctionComponent<SendgridEmailProps> = ({
    configuration,
    onSpecChange,
    onDeleteClick,
}) => {
    const { t } = useTranslation();
    const { register, errors } = useForm<SendgridEmailFormEntries>({
        mode: "onChange",
        reValidateMode: "onChange",
        defaultValues: {
            from: configuration.params.emailRecipients.from,
            apiKey: configuration.params.apiKey,
        },
        resolver: yupResolver(
            yup.object().shape({
                apiKey: yup.string().trim().required(),
                from: yup.string().trim().required(),
            })
        ),
    });

    const handleEmailListChange = (emails: string[]): void => {
        const copied = cloneDeep(configuration);
        copied.params.emailRecipients.to = emails;
        onSpecChange(copied);
    };

    return (
        <Card>
            <CardContent>
                <Grid container justifyContent="space-between">
                    <Grid item>
                        <Typography variant="h5">
                            <Icon height={24} icon="ic:twotone-email" />{" "}
                            {t("label.email")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Box textAlign="right">
                            <LocalThemeProviderV1
                                primary={lightV1.palette.error}
                            >
                                <Button
                                    color="primary"
                                    data-testid="email-delete-btn"
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
                            <EmailListInput
                                emails={configuration.params.emailRecipients.to}
                                onChange={handleEmailListChange}
                            />
                        }
                        label={t("label.add-email")}
                    />

                    <InputSection
                        helperLabel={`(${t("label.optional")})`}
                        inputComponent={
                            <TextField
                                fullWidth
                                data-testid="api-key-input-container"
                                error={Boolean(errors && errors.apiKey)}
                                helperText={
                                    errors &&
                                    errors.apiKey &&
                                    errors.apiKey.message
                                }
                                inputRef={register}
                                name="apiKey"
                                type="string"
                                variant="outlined"
                                onChange={(e) => {
                                    const copied = cloneDeep(configuration);
                                    copied.params.apiKey =
                                        e.currentTarget.value;
                                    onSpecChange(copied);
                                }}
                            />
                        }
                        label={t("label.sendgrid-api-key")}
                    />

                    <InputSection
                        helperLabel={`(${t("label.optional")})`}
                        inputComponent={
                            <TextField
                                fullWidth
                                data-testid="from-input-container"
                                error={Boolean(errors && errors.from)}
                                helperText={
                                    errors && errors.from && errors.from.message
                                }
                                inputRef={register}
                                name="from"
                                type="string"
                                variant="outlined"
                                onChange={(e) => {
                                    const copied = cloneDeep(configuration);
                                    copied.params.emailRecipients.from =
                                        e.currentTarget.value;
                                    onSpecChange(copied);
                                }}
                            />
                        }
                        label={t("label.from")}
                    />
                </Grid>
            </CardContent>
        </Card>
    );
};
