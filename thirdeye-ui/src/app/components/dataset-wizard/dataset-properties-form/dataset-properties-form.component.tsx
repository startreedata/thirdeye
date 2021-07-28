import { yupResolver } from "@hookform/resolvers/yup";
import { Grid, TextField } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { createEmptyDataset } from "../../../utils/datasets/datasets.util";
import { DatasetPropertiesFormProps } from "./dataset-properties-form.interfaces";

export const DatasetPropertiesForm: FunctionComponent<DatasetPropertiesFormProps> = (
    props: DatasetPropertiesFormProps
) => {
    const { t } = useTranslation();
    const { register, handleSubmit, errors } = useForm<Dataset>({
        mode: "onSubmit",
        reValidateMode: "onSubmit",
        defaultValues: props.dataset || createEmptyDataset(),
        resolver: yupResolver(
            yup.object().shape({
                name: yup
                    .string()
                    .trim()
                    .required(t("message.dataset-name-required")),
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
            </Grid>
        </form>
    );
};
