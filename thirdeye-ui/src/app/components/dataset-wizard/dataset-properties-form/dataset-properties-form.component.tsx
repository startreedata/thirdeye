// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { yupResolver } from "@hookform/resolvers/yup";
import {
    Box,
    Button,
    FormControl,
    FormHelperText,
    Grid,
    MenuItem,
    Select,
    TextField,
} from "@material-ui/core";
import _ from "lodash";
import React, { FunctionComponent } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../platform/components";
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { DatasourceVerification } from "../datasource-verification/datasource-verification.component";
import { DatasetPropertiesFormProps } from "./dataset-properties-form.interfaces";

export const DatasetPropertiesForm: FunctionComponent<DatasetPropertiesFormProps> =
    ({ id, dataset, datasources, onSubmit, onCancel, submitBtnLabel }) => {
        const { t } = useTranslation();
        const { register, handleSubmit, errors, control, watch } =
            useForm<Dataset>({
                defaultValues: dataset,
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
                                .required(
                                    t("message.dataset-datasource-required")
                                ),
                        }),
                    })
                ),
            });
        const dataSourceProperty = watch("dataSource");

        const onSubmitDatasetPropertiesForm = (dataset: Dataset): void => {
            onSubmit && onSubmit(dataset);
        };

        return (
            <form
                noValidate
                id={id}
                onSubmit={handleSubmit(onSubmitDatasetPropertiesForm)}
            >
                <PageContentsGridV1>
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <Grid container>
                                <InputSection
                                    inputComponent={
                                        <TextField
                                            fullWidth
                                            required
                                            error={Boolean(
                                                errors && errors.name
                                            )}
                                            helperText={
                                                errors &&
                                                errors.name &&
                                                errors.name.message
                                            }
                                            inputRef={register}
                                            name="name"
                                            type="string"
                                            variant="outlined"
                                        />
                                    }
                                    label={t("label.name")}
                                />

                                <InputSection
                                    inputComponent={
                                        <>
                                            <FormControl
                                                fullWidth
                                                error={Boolean(
                                                    errors.dataSource?.name
                                                        ?.message
                                                )}
                                                size="small"
                                                variant="outlined"
                                            >
                                                <Controller
                                                    control={control}
                                                    name="dataSource"
                                                    render={({
                                                        onChange,
                                                        value,
                                                    }) => (
                                                        <Select
                                                            fullWidth
                                                            value={value.name}
                                                            variant="outlined"
                                                            onChange={(e) =>
                                                                onChange({
                                                                    name: e
                                                                        .target
                                                                        .value,
                                                                })
                                                            }
                                                        >
                                                            {!_.isEmpty(
                                                                datasources
                                                            ) &&
                                                                datasources.map(
                                                                    (
                                                                        datasource,
                                                                        index
                                                                    ) => (
                                                                        <MenuItem
                                                                            key={
                                                                                index
                                                                            }
                                                                            value={
                                                                                datasource.name
                                                                            }
                                                                        >
                                                                            {
                                                                                datasource.name
                                                                            }
                                                                        </MenuItem>
                                                                    )
                                                                )}
                                                        </Select>
                                                    )}
                                                />
                                                {errors.dataSource?.name && (
                                                    <FormHelperText error>
                                                        {
                                                            errors.dataSource
                                                                .name.message
                                                        }
                                                    </FormHelperText>
                                                )}
                                                {dataSourceProperty.name && (
                                                    <DatasourceVerification
                                                        datasourceName={
                                                            dataSourceProperty.name
                                                        }
                                                    />
                                                )}
                                            </FormControl>
                                        </>
                                    }
                                    label={t("label.datasource")}
                                />
                            </Grid>
                        </PageContentsCardV1>
                    </Grid>
                </PageContentsGridV1>

                <Box textAlign="right" width="100%">
                    <PageContentsCardV1>
                        <Grid container justifyContent="flex-end">
                            <Grid item>
                                <Button
                                    color="secondary"
                                    onClick={() => onCancel && onCancel()}
                                >
                                    {t("label.cancel")}
                                </Button>
                            </Grid>
                            <Grid item>
                                <Button color="primary" type="submit">
                                    {submitBtnLabel}
                                </Button>
                            </Grid>
                        </Grid>
                    </PageContentsCardV1>
                </Box>
            </form>
        );
    };
