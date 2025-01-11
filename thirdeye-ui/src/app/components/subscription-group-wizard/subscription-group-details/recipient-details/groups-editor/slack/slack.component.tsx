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
    IconButton,
    TextField,
    Tooltip,
    Typography,
    useTheme,
} from "@material-ui/core";
import AddCircleOutlineIcon from "@material-ui/icons/AddCircleOutline";
import RemoveCircleOutline from "@material-ui/icons/RemoveCircleOutline";
import React, { FunctionComponent } from "react";
import { Controller, useFieldArray, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { LocalThemeProviderV1 } from "../../../../../../platform/components";
import { SpecType } from "../../../../../../rest/dto/subscription-group.interfaces";
import {
    convertSlackConfigurationToSlackFormEntries,
    validateSlackMemberIDFormat,
} from "../../../../../../utils/notifications/notifications.util";
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
        defaultValues: convertSlackConfigurationToSlackFormEntries(
            configuration.params
        ),
        resolver: yupResolver(
            yup.object().shape({
                webhookUrl: yup
                    .string()
                    .trim()
                    .required(t("message.url-required")),
                notifyResolvedAnomalies: yup.boolean().optional(),
                textConfiguration: yup.object().shape({
                    owner: yup.string().required("Owner is required"),
                    mentionMemberIds: yup.array().of(
                        yup.object().shape({
                            value: yup
                                .string()
                                .test(
                                    "is-valid-slack-member-id",
                                    t("message.invalid-slack-id"),
                                    (value) => {
                                        if (!value) {
                                            return true;
                                        }

                                        return validateSlackMemberIDFormat(
                                            value
                                        );
                                    }
                                ),
                        })
                    ),
                }),
            })
        ),
    });

    // useFieldArray for storing textConfiguration.mentionMemberIds
    const { fields, append, remove } = useFieldArray({
        control,
        name: "textConfiguration.mentionMemberIds",
    });

    const handleWebhookUrlChange = (newValue: string): void => {
        const copied = {
            ...configuration,
        };
        copied.params.webhookUrl = newValue;

        onSpecChange(copied);
    };

    const handleTextConfigurationOwnerChange = (newValue: string): void => {
        const copied = {
            ...configuration,
        };
        if (copied.params.textConfiguration?.owner) {
            copied.params.textConfiguration.owner = newValue;
        } else {
            copied.params.textConfiguration = {
                ...(copied.params.textConfiguration || {}),
                owner: newValue,
            };
        }

        onSpecChange(copied);
    };

    const handleNotifyResolvedAnomaliesChange = (newValue: boolean): void => {
        const copied = {
            ...configuration,
        };
        copied.params.notifyResolvedAnomalies = newValue;

        onSpecChange(copied);
    };

    const handleOneMessagePerAnomalyChange = (newValue: boolean): void => {
        const copied = {
            ...configuration,
        };
        copied.params.sendOneMessagePerAnomaly = newValue;

        onSpecChange(copied);
    };

    const handleMentionMemberIdsChange = (
        newValue: string,
        index: number
    ): void => {
        const copied = {
            ...configuration,
        };
        if (copied.params.textConfiguration?.mentionMemberIds) {
            copied.params.textConfiguration.mentionMemberIds[index] = newValue;
        } else {
            copied.params.textConfiguration = {
                ...(copied.params.textConfiguration || {}),
                mentionMemberIds: [],
            };
            copied.params.textConfiguration.mentionMemberIds[index] = newValue;
        }

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
                    <InputSection
                        inputComponent={
                            <TextField
                                fullWidth
                                data-testid="slack-owner-container"
                                error={Boolean(
                                    errors && errors.textConfiguration?.owner
                                )}
                                helperText={
                                    errors &&
                                    errors.textConfiguration?.owner &&
                                    errors.textConfiguration?.owner?.message
                                }
                                inputProps={register("textConfiguration.owner")}
                                name="textConfiguration.owner"
                                type="string"
                                variant="outlined"
                                onChange={(e) =>
                                    handleTextConfigurationOwnerChange(
                                        e.currentTarget.value
                                    )
                                }
                            />
                        }
                        label={t("label.slack-owner")}
                    />
                    <InputSection
                        gridContainerProps={{ alignItems: "flex-start" }}
                        helperLabel={t("label.slack-member-ids-helper-text")}
                        inputComponent={
                            <Box>
                                {fields.map((field, index) => (
                                    <Box
                                        alignItems="center"
                                        display="flex"
                                        key={field.id}
                                        marginBottom={2}
                                    >
                                        <TextField
                                            fullWidth
                                            error={Boolean(
                                                errors.textConfiguration
                                                    ?.mentionMemberIds?.[index]
                                                    ?.value
                                            )}
                                            helperText={
                                                errors.textConfiguration
                                                    ?.mentionMemberIds?.[index]
                                                    ?.value?.message
                                            }
                                            inputProps={register(
                                                `textConfiguration.mentionMemberIds.${index}.value`
                                            )}
                                            name={`textConfiguration.mentionMemberIds.${index}.value`}
                                            variant="outlined"
                                            onChange={(e) =>
                                                handleMentionMemberIdsChange(
                                                    e.currentTarget.value,
                                                    index
                                                )
                                            }
                                        />
                                        <IconButton
                                            onClick={() => remove(index)}
                                        >
                                            <RemoveCircleOutline color="error" />
                                        </IconButton>
                                    </Box>
                                ))}
                                <Box
                                    sx={{
                                        display: "flex",
                                        justifyContent: "center",
                                    }}
                                >
                                    <Tooltip
                                        title={t(
                                            fields.length
                                                ? "label.add-another-slack-member-id"
                                                : "label.add-slack-member-id"
                                        )}
                                    >
                                        <IconButton
                                            onClick={() =>
                                                append({ value: "" })
                                            }
                                        >
                                            <AddCircleOutlineIcon color="primary" />
                                        </IconButton>
                                    </Tooltip>
                                </Box>
                            </Box>
                        }
                        label={t("label.slack-member-ids")}
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
                    <Controller
                        control={control}
                        name="sendOneMessagePerAnomaly"
                        render={({ field: { name, value, onChange } }) => (
                            <InputSection
                                inputComponent={
                                    <FormControlLabel
                                        checked={value}
                                        control={<Checkbox color="primary" />}
                                        label={
                                            <Typography variant="body2">
                                                {t(
                                                    "message.send-separate-slack-messages"
                                                )}
                                            </Typography>
                                        }
                                        name={name}
                                        onChange={(_e, checked) => {
                                            onChange(checked);
                                            handleOneMessagePerAnomalyChange(
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
