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
import { PagerDutyFormEntries, PagerDutyProps } from "./pager-duty.interfaces";

export const PagerDuty: FunctionComponent<PagerDutyProps> = ({
    configuration,
    onSpecChange,
    onDeleteClick,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();
    const {
        register,
        formState: { errors },
    } = useForm<PagerDutyFormEntries>({
        mode: "onChange",
        reValidateMode: "onChange",
        defaultValues: configuration.params,
        resolver: yupResolver(
            yup.object().shape({
                url: yup
                    .string()
                    .trim()
                    .required(t("message.events-integration-key-required")),
            })
        ),
    });

    const handleUrlChange = (newValue: string): void => {
        const copied = {
            ...configuration,
        };
        copied.params.eventsIntegrationKey = newValue;

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
                                            SpecType.PagerDuty
                                        ]
                                    }
                                />{" "}
                                {t(
                                    subscriptionGroupChannelHeaderMap[
                                        SpecType.PagerDuty
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
                                    data-testid="pager-duty-delete-btn"
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
                                    data-testid="pager-duty-input-container"
                                    error={Boolean(
                                        errors && errors.eventsIntegrationKey
                                    )}
                                    helperText={
                                        errors &&
                                        errors.eventsIntegrationKey &&
                                        errors.eventsIntegrationKey.message
                                    }
                                    inputProps={register(
                                        "eventsIntegrationKey"
                                    )}
                                    name="eventsIntegrationKey"
                                    type="string"
                                    variant="outlined"
                                    onChange={(e) =>
                                        handleUrlChange(e.currentTarget.value)
                                    }
                                />
                                <FormHelperText>
                                    {t("label.eg")}{" "}
                                    ad2a75369ae44008c051dfb8323d4285
                                </FormHelperText>
                            </>
                        }
                        label={t("label.integration-key")}
                    />
                </Grid>
            </CardContent>
        </Card>
    );
};
