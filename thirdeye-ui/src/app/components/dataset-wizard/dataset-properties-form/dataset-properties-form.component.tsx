import { yupResolver } from "@hookform/resolvers/yup";
import {
    Box,
    FormControl,
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
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { createEmptyDataset } from "../../../utils/datasets/datasets.util";
import { DatasetPropertiesFormProps } from "./dataset-properties-form.interfaces";

export const DatasetPropertiesForm: FunctionComponent<DatasetPropertiesFormProps> = (
    props: DatasetPropertiesFormProps
) => {
    const { t } = useTranslation();
    const defaultValues = props.dataset || createEmptyDataset();
    const { register, handleSubmit, errors, control } = useForm<Dataset>({
        defaultValues,
        resolver: yupResolver(
            yup.object().shape({
                name: yup
                    .string()
                    .trim()
                    .required(t("message.dataset-name-required")),
                dataSource: yup.object().shape({
                    name: yup
                        .string()
                        .trim()
                        .required(t("message.dataset-datasource-required")),
                }),
            })
        ),
    });

    const onSubmitDatasetPropertiesForm = (dataset: Dataset): void => {
        props.onSubmit && props.onSubmit(dataset);
    };

    return (
        <form
            noValidate
            id={props.id}
            onSubmit={handleSubmit(onSubmitDatasetPropertiesForm)}
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

                {/* Datasource label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.datasource")}
                    </Typography>
                </Grid>

                {/* Datasource input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
                    <FormControl
                        fullWidth
                        error={Boolean(errors.dataSource?.name?.message)}
                        size="small"
                        variant="outlined"
                    >
                        <Controller
                            control={control}
                            name="dataSource"
                            render={({ onChange, value }) => (
                                <Select
                                    fullWidth
                                    value={value.name}
                                    variant="outlined"
                                    onChange={(e) =>
                                        onChange({ name: e.target.value })
                                    }
                                >
                                    {!_.isEmpty(props.datasources) &&
                                        props.datasources.map(
                                            (datasource, index) => (
                                                <MenuItem
                                                    key={index}
                                                    value={datasource.name}
                                                >
                                                    {datasource.name}
                                                </MenuItem>
                                            )
                                        )}
                                </Select>
                            )}
                        />
                        {errors.dataSource?.name && (
                            <FormHelperText error>
                                {errors.dataSource.name.message}
                            </FormHelperText>
                        )}
                    </FormControl>
                </Grid>
            </Grid>
        </form>
    );
};
