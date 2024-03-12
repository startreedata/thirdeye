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
    Checkbox,
    FormControlLabel,
    FormHelperText,
    Grid,
    TextField,
    Typography,
    useTheme,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { LocalThemeProviderV1 } from "../../../../../../platform/components";
import { SpecType } from "../../../../../../rest/dto/subscription-group.interfaces";
import { InputSection } from "../../../../../form-basics/input-section/input-section.component";
import {
    subscriptionGroupChannelHeaderMap,
    subscriptionGroupChannelIconsMap,
} from "../../../../../subscription-group-view/notification-channels-card/notification-channels-card.utils";
import { SlackFormEntries, SlackProps } from "./slack.interfaces";

export const Slack: FunctionComponent<SlackProps> = ({
    configuration,
    onSpecChange,
    onDeleteClick,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();
    const {
        register,
        formState: { errors },
        control,
    } = useForm<SlackFormEntries>({
        mode: "onChange",
        reValidateMode: "onChange",
        defaultValues: configuration.params,
        resolver: yupResolver(
            yup.object().shape({
                webhookUrl: yup
                    .string()
                    .trim()
                    .required(t("message.url-required")),
                notifyResolvedAnomalies: yup.boolean().optional(),
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

    const handleNotifyResolvedAnomaliesChange = (newValue: boolean): void => {
        const copied = {
            ...configuration,
        };
        copied.params.notifyResolvedAnomalies = newValue;

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
                                            SpecType.Slack
                                        ]
                                    }
                                />
                                &nbsp;
                                {t(
                                    subscriptionGroupChannelHeaderMap[
                                        SpecType.Slack
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
                                    data-testid="slack-delete-btn"
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
                                    inputProps={register("webhookUrl")}
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
                                    https://hooks.slack.com/services/T1234567/AAAAAAAA/ZZZZZZ
                                </FormHelperText>
                            </>
                        }
                        label={t("label.slack-url")}
                    />
                </Grid>
                <Grid container>
                    <Controller
                        control={control}
                        name="notifyResolvedAnomalies"
                        render={({ field: { name, value, onChange } }) => (
                            <InputSection
                                inputComponent={
                                    <FormControlLabel
                                        // eslint-disable-next-line max-len
                                        // `control` needs to be passed on differently for checkbox
                                        // eslint-disable-next-line max-len
                                        // https://stackoverflow.com/questions/62291962/react-hook-forms-material-ui-checkboxes
                                        checked={value}
                                        control={<Checkbox color="primary" />}
                                        label={
                                            <Typography variant="body2">
                                                {t(
                                                    "message.notify-when-the-anomaly-period-ends"
                                                )}
                                            </Typography>
                                        }
                                        name={name}
                                        onChange={(_e, checked) => {
                                            onChange(checked);
                                            handleNotifyResolvedAnomaliesChange(
                                                checked
                                            );
                                        }}
                                    />
                                }
                            />
                        )}
                    />
                </Grid>
            </CardContent>
        </Card>
    );
};
