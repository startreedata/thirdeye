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
    useTheme,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { LocalThemeProviderV1 } from "../../../../../../platform/components";
import { SpecType } from "../../../../../../rest/dto/subscription-group.interfaces";
import { InputSection } from "../../../../../form-basics/input-section/input-section.component";
import {
    subscriptionGroupChannelHeaderMap,
    subscriptionGroupChannelIconsMap,
} from "../../../../../subscription-group-view/notification-channels-card/notification-channels-card.utils";
import { WebhookFormEntries, WebhookProps } from "./webhook.interfaces";

export const Webhook: FunctionComponent<WebhookProps> = ({
    configuration,
    onSpecChange,
    onDeleteClick,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();
    const {
        register,
        formState: { errors },
    } = useForm<WebhookFormEntries>({
        mode: "onChange",
        reValidateMode: "onChange",
        defaultValues: configuration.params,
        resolver: yupResolver(
            yup.object().shape({
                url: yup.string().trim().required(t("message.url-required")),
            })
        ),
    });

    const handleUrlChange = (newValue: string): void => {
        const copied = {
            ...configuration,
        };
        copied.params.url = newValue;

        onSpecChange(copied);
    };

    return (
        <Card>
            <CardContent>
                <Grid container justifyContent="space-between">
                    <Grid item>
                        <Box clone alignItems="center" display="flex">
                            <Typography variant="h5">
                                <Icon
                                    color={theme.palette.primary.main}
                                    height={28}
                                    icon={
                                        subscriptionGroupChannelIconsMap[
                                            SpecType.Webhook
                                        ]
                                    }
                                />
                                &nbsp;
                                {t(
                                    subscriptionGroupChannelHeaderMap[
                                        SpecType.Webhook
                                    ]
                                )}
                            </Typography>
                        </Box>
                    </Grid>
                    <Grid item>
                        <Box textAlign="right">
                            <LocalThemeProviderV1 primary={theme.palette.error}>
                                <Button
                                    color="primary"
                                    data-testid="webhook-delete-btn"
                                    variant="outlined"
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
                <Grid>
                    <InputSection
                        inputComponent={
                            <>
                                <TextField
                                    fullWidth
                                    data-testid="webhook-input-container"
                                    error={Boolean(errors && errors.url)}
                                    helperText={
                                        errors &&
                                        errors.url &&
                                        errors.url.message
                                    }
                                    inputProps={register("url")}
                                    name="url"
                                    type="string"
                                    variant="outlined"
                                    onChange={(e) =>
                                        handleUrlChange(e.currentTarget.value)
                                    }
                                />
                                <FormHelperText>
                                    e.g.
                                    https://hooks.com/services/T000000/B000000/XXXXX
                                </FormHelperText>
                            </>
                        }
                        label={t("label.webhook-url")}
                    />
                </Grid>
            </CardContent>
        </Card>
    );
};
