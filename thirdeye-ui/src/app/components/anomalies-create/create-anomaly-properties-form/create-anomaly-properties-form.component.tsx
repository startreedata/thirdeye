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

import { Box, Grid, TextField, Typography } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { capitalize } from "lodash";
import React, { FunctionComponent, ReactNode, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { LinkV1, SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../platform/rest/actions.interfaces";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { generateNameForEnumerationItem } from "../../../utils/enumeration-items/enumeration-items.util";
import { getAlertsAlertViewPath } from "../../../utils/routes/routes.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { useCreateAnomalyWizardStyles } from "../create-anomaly-wizard/create-anomaly-wizard.styles";
import { getEnumerationItemsConfigFromAlert } from "../create-anomaly-wizard/create-anomaly-wizard.utils";
import { CreateAnomalyPropertiesFormProps } from "./create-anomaly-properties-form.interfaces";

export const CreateAnomalyPropertiesForm: FunctionComponent<CreateAnomalyPropertiesFormProps> =
    ({
        alerts,
        formFields,
        handleSetField,
        enumerationItemsForAlert,
        enumerationItemsStatus,
        selectedAlertDetails,
    }) => {
        const { t } = useTranslation();
        const classes = useCreateAnomalyWizardStyles();

        const showEnumerationItemsField = !!(
            formFields.alert &&
            getEnumerationItemsConfigFromAlert(formFields.alert)
        );

        const renderAlertOption = useCallback(
            (option: Alert): ReactNode => {
                const {
                    dataSource,
                    dataset,
                    aggregationFunction,
                    aggregationColumn,
                } = option.templateProperties as {
                    dataSource: string;
                    dataset: string;
                    aggregationFunction: string;
                    aggregationColumn: string;
                };
                const metric = `${aggregationFunction}(${aggregationColumn})`;

                return (
                    <div>
                        <Typography variant="h6">{option.name}</Typography>
                        {[
                            [t("label.datasource"), dataSource],
                            [t("label.dataset"), dataset],
                            [t("label.metric"), metric],
                        ].map(([label, value]) => (
                            <Typography
                                color="textSecondary"
                                display="block"
                                key={label}
                                variant="caption"
                            >
                                <strong>{label}</strong>: {value}
                            </Typography>
                        ))}
                    </div>
                );
            },
            [alerts]
        );

        return (
            <>
                <InputSection
                    inputComponent={
                        <Autocomplete
                            // This is to avoid having to switch this input from
                            // uncontrollable to controllable (react frowns at that)
                            // Issue link:
                            // https://github.com/mui/material-ui
                            // /issues/29046#issuecomment-1379770362
                            disableClearable={!!formFields.alert} // Only disable clearing once the value is loaded
                            getOptionLabel={(option) => option.name}
                            options={alerts}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    InputProps={{
                                        ...params.InputProps,
                                        // Override class name so the size of input is smaller
                                        className: classes.autoCompleteInput,
                                    }}
                                    {...(!!selectedAlertDetails && {
                                        helperText: (
                                            <Box ml={-1}>
                                                <Typography
                                                    color="secondary"
                                                    variant="caption"
                                                >
                                                    <Grid container spacing={1}>
                                                        {[
                                                            [
                                                                t(
                                                                    "label.datasource"
                                                                ),
                                                                selectedAlertDetails.dataSource,
                                                            ],
                                                            [
                                                                t(
                                                                    "label.dataset"
                                                                ),
                                                                selectedAlertDetails.dataset,
                                                            ],
                                                            [
                                                                t(
                                                                    "label.metric"
                                                                ),
                                                                selectedAlertDetails.metric,
                                                            ],
                                                        ].map(
                                                            ([
                                                                label,
                                                                value,
                                                            ]) => (
                                                                <Grid
                                                                    item
                                                                    key={value}
                                                                    spacing={1}
                                                                >
                                                                    <strong>
                                                                        {label}
                                                                    </strong>
                                                                    : {value}
                                                                </Grid>
                                                            )
                                                        )}
                                                    </Grid>
                                                </Typography>
                                            </Box>
                                        ),
                                    })}
                                    margin="normal"
                                    placeholder={capitalize(
                                        t(
                                            "message.click-here-to-select-entity",
                                            {
                                                entity: t("label.alert"),
                                            }
                                        )
                                    )}
                                    size="small"
                                    variant="outlined"
                                />
                            )}
                            renderOption={renderAlertOption}
                            value={formFields.alert}
                            onChange={(_, selectedValue) => {
                                selectedValue &&
                                    handleSetField("alert", selectedValue);
                            }}
                        />
                    }
                    labelComponent={
                        <>
                            <Typography variant="body2">
                                {t("label.alert")}
                            </Typography>
                            {!!formFields.alert && (
                                <LinkV1
                                    externalLink
                                    href={getAlertsAlertViewPath(
                                        formFields.alert.id
                                    )}
                                    target="_blank"
                                    variant="caption"
                                >
                                    {t("label.view-entity", {
                                        entity: t("label.alert"),
                                    })}
                                </LinkV1>
                            )}
                        </>
                    }
                />
                <LoadingErrorStateSwitch
                    isError={enumerationItemsStatus === ActionStatus.Error}
                    isLoading={enumerationItemsStatus === ActionStatus.Working}
                    loadingState={
                        <InputSection
                            inputComponent={
                                <SkeletonV1
                                    height={40}
                                    variant="rect"
                                    width="100%"
                                />
                            }
                            label={t("label.dimension")}
                        />
                    }
                >
                    {!!(
                        showEnumerationItemsField &&
                        enumerationItemsForAlert &&
                        enumerationItemsForAlert.length > 0
                    ) && (
                        <InputSection
                            inputComponent={
                                <Autocomplete
                                    disableClearable={
                                        !!formFields.enumerationItem
                                    }
                                    getOptionLabel={(option) =>
                                        generateNameForEnumerationItem(option)
                                    }
                                    options={enumerationItemsForAlert}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            InputProps={{
                                                ...params.InputProps,
                                                // Override class name so the
                                                // size of input is smaller
                                                className:
                                                    classes.autoCompleteInput,
                                            }}
                                            placeholder={capitalize(
                                                t(
                                                    "message.click-here-to-select-entity",
                                                    {
                                                        entity: t(
                                                            "label.dimension"
                                                        ),
                                                    }
                                                )
                                            )}
                                            variant="outlined"
                                        />
                                    )}
                                    size="small"
                                    value={formFields.enumerationItem}
                                    onChange={(_, selectedValue) => {
                                        handleSetField(
                                            "enumerationItem",
                                            selectedValue
                                        );
                                    }}
                                />
                            }
                            label={t("label.dimension")}
                        />
                    )}
                </LoadingErrorStateSwitch>
            </>
        );
    };
