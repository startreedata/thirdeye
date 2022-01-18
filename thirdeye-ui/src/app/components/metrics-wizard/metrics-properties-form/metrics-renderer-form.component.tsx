import { yupResolver } from "@hookform/resolvers/yup";
import {
    Box,
    Checkbox,
    FormControl,
    FormControlLabel,
    FormHelperText,
    Grid,
    MenuItem,
    Select,
    TextField,
    Typography,
} from "@material-ui/core";
import _ from "lodash";
import React, { FunctionComponent } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { LogicalMetric } from "../../../rest/dto/metric.interfaces";
import { createEmptyMetric } from "../../../utils/metrics/metrics.util";
import { MetricsPropertiesFormProps } from "./metrics-renderer-form.interfaces";

export const MetricsPropertiesForm: FunctionComponent<MetricsPropertiesFormProps> = (
    props: MetricsPropertiesFormProps
) => {
    const { t } = useTranslation();
    const defaultValues = props.metric || createEmptyMetric();
    const { register, handleSubmit, errors, control } = useForm<LogicalMetric>({
        defaultValues,
        resolver: yupResolver(
            yup.object().shape({
                name: yup
                    .string()
                    .trim()
                    .required(t("message.metrics-name-required")),
                rollupThreshold: yup
                    .number()
                    .typeError(t("message.metrics-threshold-required"))
                    .required(t("message.metrics-threshold-required")),
                aggregationFunction: yup
                    .string()
                    .trim()
                    .required(
                        t("message.metrics-aggregation-function-required")
                    ),
                active: yup.boolean(),
                dataset: yup.object().shape({
                    name: yup
                        .string()
                        .trim()
                        .required(t("message.metrics-dataset-required")),
                }),
            })
        ),
    });

    const onSubmitSusbcriptionGroupPropertiesForm = (
        metric: LogicalMetric
    ): void => {
        if (!props.onSubmit) {
            return;
        }

        const dataset = props.datasets.find(
            (dataset) => dataset.name === metric.dataset?.name
        );

        if (metric.dataset) {
            props.onSubmit({
                ...metric,
                dataset,
            });
        } else {
            props.onSubmit(metric);
        }
    };

    return (
        <form
            noValidate
            id={props.id}
            onSubmit={handleSubmit(onSubmitSusbcriptionGroupPropertiesForm)}
        >
            <Grid container alignItems="center">
                {/* Name label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.name")}
                    </Typography>
                </Grid>

                {/* Name input */}
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

                {/* Agreegation Function label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.aggregation-function")}
                    </Typography>
                </Grid>

                {/* Aggregation Function Input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
                    <TextField
                        fullWidth
                        required
                        error={Boolean(errors && errors.aggregationFunction)}
                        helperText={
                            errors &&
                            errors.aggregationFunction &&
                            errors.aggregationFunction.message
                        }
                        inputRef={register}
                        name="aggregationFunction"
                        type="string"
                        variant="outlined"
                    />
                </Grid>

                <Box width="100%" />

                {/* Dataset label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.dataset")}
                    </Typography>
                </Grid>

                {/* Dataset input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
                    <FormControl
                        fullWidth
                        error={Boolean(errors.dataset?.name?.message)}
                        size="small"
                        variant="outlined"
                    >
                        <Controller
                            control={control}
                            name="dataset"
                            render={({ onChange, value }) => (
                                <Select
                                    fullWidth
                                    value={value.name}
                                    variant="outlined"
                                    onChange={(e) =>
                                        onChange({ name: e.target.value })
                                    }
                                >
                                    {!_.isEmpty(props.datasets) &&
                                        props.datasets.map((dataset, index) => (
                                            <MenuItem
                                                key={index}
                                                value={dataset.name}
                                            >
                                                {dataset.name}
                                            </MenuItem>
                                        ))}
                                </Select>
                            )}
                        />
                        {errors.dataset?.name && (
                            <FormHelperText error>
                                {errors.dataset.name.message}
                            </FormHelperText>
                        )}
                    </FormControl>
                </Grid>

                <Box width="100%" />

                {/* Threshold label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.threshold")}
                    </Typography>
                </Grid>

                {/* Threshold input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
                    <TextField
                        fullWidth
                        required
                        error={Boolean(errors && errors.rollupThreshold)}
                        helperText={
                            errors &&
                            errors.rollupThreshold &&
                            errors.rollupThreshold.message
                        }
                        inputRef={register}
                        name="rollupThreshold"
                        type="number"
                        variant="outlined"
                    />
                </Grid>
            </Grid>

            {/* Active label */}
            <Grid container item sm={12}>
                <Grid item sm={12}>
                    <FormControlLabel
                        control={
                            <Controller
                                control={control}
                                inputRef={register}
                                name="active"
                                render={({ onChange, value }) => (
                                    <Checkbox
                                        checked={value}
                                        color="primary"
                                        onChange={(e) =>
                                            onChange(e.target.checked)
                                        }
                                    />
                                )}
                            />
                        }
                        label={
                            <Typography variant="body2">
                                {t("label.active")}
                            </Typography>
                        }
                    />
                </Grid>
            </Grid>
        </form>
    );
};
