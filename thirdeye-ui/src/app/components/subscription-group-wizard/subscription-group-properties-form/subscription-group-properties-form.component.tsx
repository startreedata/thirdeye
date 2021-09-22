import { yupResolver } from "@hookform/resolvers/yup";
import { Grid, TextField } from "@material-ui/core";
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
            <Grid container>
                <Grid item sm={6}>
                    {/* Name */}
                    <TextField
                        fullWidth
                        required
                        error={Boolean(errors && errors.name)}
                        helperText={
                            errors && errors.name && errors.name.message
                        }
                        inputRef={register}
                        label={t("label.name")}
                        name="name"
                        type="string"
                        variant="outlined"
                    />
                </Grid>
                <Grid item sm={6}>
                    {/* Schedule (or Cron) */}
                    <TextField
                        fullWidth
                        inputRef={register}
                        label={t("label.cron")}
                        name="cron"
                        type="string"
                        variant="outlined"
                    />
                </Grid>
            </Grid>
        </form>
    );
};
