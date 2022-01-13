import { yupResolver } from "@hookform/resolvers/yup";
import { Box, Grid, TextField, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";
import { createEmptySubscriptionGroup } from "../../../utils/subscription-groups/subscription-groups.util";
import { SubscriptionGroupPropertiesFormProps } from "./subscription-group-properties-form.interfaces";

export const SubscriptionGroupPropertiesForm: FunctionComponent<SubscriptionGroupPropertiesFormProps> = (
    props: SubscriptionGroupPropertiesFormProps
) => {
    const { t } = useTranslation();
    const { register, handleSubmit, errors } = useForm<SubscriptionGroup>({
        mode: "onSubmit",
        reValidateMode: "onSubmit",
        defaultValues:
            props.subscriptionGroup || createEmptySubscriptionGroup(),
        resolver: yupResolver(
            yup.object().shape({
                name: yup
                    .string()
                    .trim()
                    .required(t("message.subscription-group-name-required")),
            })
        ),
    });

    const onSubmitSusbcriptionGroupPropertiesForm = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        props.onSubmit && props.onSubmit(subscriptionGroup);
    };

    return (
        <form
            noValidate
            id={props.id}
            onSubmit={handleSubmit(onSubmitSusbcriptionGroupPropertiesForm)}
        >
            <Grid container alignItems="center">
                {/* Subscription group name label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.name")}
                    </Typography>
                </Grid>

                {/* Subscription group name input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
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
                    />
                </Grid>

                <Box width="100%" />

                {/* Schedule (or Cron) label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.cron")}
                    </Typography>
                </Grid>

                {/* Schedule (or Cron) input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
                    <TextField
                        fullWidth
                        inputRef={register}
                        name="cron"
                        type="string"
                        variant="outlined"
                    />
                </Grid>
            </Grid>
        </form>
    );
};
