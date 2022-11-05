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
import { FormHelperText, Grid, Link, TextField } from "@material-ui/core";
import CronValidator from "cron-expression-validator";
import cronstrue from "cronstrue";
import React, { FunctionComponent } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { SubscriptionGroupPropertiesFormProps } from "./subscription-group-properties-form.interfaces";

export const SubscriptionGroupPropertiesForm: FunctionComponent<SubscriptionGroupPropertiesFormProps> =
    ({ subscriptionGroup, onChange }) => {
        const { t } = useTranslation();
        const { register, errors, watch } = useForm<SubscriptionGroup>({
            mode: "onChange",
            reValidateMode: "onChange",
            defaultValues: subscriptionGroup,
            resolver: yupResolver(
                yup.object().shape({
                    name: yup
                        .string()
                        .trim()
                        .required(
                            t("message.subscription-group-name-required")
                        ),
                })
            ),
        });
        const cron = watch("cron");
        const isCronValid = CronValidator.isValidCronExpression(cron);

        return (
            <Grid container>
                <InputSection
                    inputComponent={
                        <TextField
                            fullWidth
                            required
                            error={Boolean(errors && errors.name)}
                            helperText={
                                errors && errors.name && errors.name.message
                            }
                            inputRef={register}
                            name="name"
                            type="string"
                            variant="outlined"
                            onChange={(e) => {
                                onChange({
                                    name: e.currentTarget.value,
                                });
                            }}
                        />
                    }
                    label={t("label.name")}
                />

                <InputSection
                    inputComponent={
                        <>
                            <TextField
                                fullWidth
                                error={!isCronValid}
                                inputRef={register}
                                name="cron"
                                type="string"
                                variant="outlined"
                                onChange={(e) => {
                                    onChange({
                                        cron: e.currentTarget.value,
                                    });
                                }}
                            />
                            {isCronValid && (
                                <FormHelperText>
                                    {cronstrue.toString(cron, {
                                        verbose: true,
                                    })}
                                </FormHelperText>
                            )}
                            {!isCronValid && (
                                <FormHelperText
                                    error
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
                    label={t("label.schedule")}
                />
            </Grid>
        );
    };
