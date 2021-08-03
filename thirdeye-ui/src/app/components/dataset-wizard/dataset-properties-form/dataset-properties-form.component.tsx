import { yupResolver } from "@hookform/resolvers/yup";
import {
    FormControl,
    FormHelperText,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    TextField,
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
            <Grid container>
                {/* Name label */}
                <Grid container item sm={6}>
                    <Grid item sm={8}>
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
                </Grid>
                <Grid container item sm={6}>
                    <Grid item sm={8}>
                        {/* Datasource */}
                        <FormControl fullWidth variant="outlined">
                            <InputLabel id="datasets-datasource">
                                {t(`label.datasource`)}
                            </InputLabel>
                            <Controller
                                control={control}
                                name="dataSource"
                                render={({ onChange, value }) => (
                                    <Select
                                        fullWidth
                                        label={t("label.datasource")}
                                        labelId="datasets-datasource"
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
            </Grid>
        </form>
    );
};
