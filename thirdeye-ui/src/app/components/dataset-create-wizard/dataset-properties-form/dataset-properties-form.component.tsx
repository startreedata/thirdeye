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
import { Box, Divider, Grid, TextField } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { isEmpty } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { useGetTablesForDatasourceID } from "../../../rest/datasources/datasources.actions";
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { Datasource } from "../../../rest/dto/datasource.interfaces";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { SelectDatasetOption } from "../../welcome-onboard-datasource/select-dataset-option/select-dataset-option.component";
import { WizardBottomBar } from "../../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { DatasourceVerification } from "../datasource-verification/datasource-verification.component";
import {
    DATASET_FORM_TEST_IDS,
    DatasetPropertiesFormProps,
} from "./dataset-properties-form.interfaces";

export const DatasetPropertiesForm: FunctionComponent<DatasetPropertiesFormProps> =
    ({ existingDatasets, datasources, onSubmit }) => {
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();

        const {
            tables,
            getTableForDatasourceID,
            status: getTablesStatus,
            errorMessages: getTableForDatasourceNameErrors,
        } = useGetTablesForDatasourceID();

        const [selectedDataSource, setSelectedDataSource] =
            useState<Datasource>();
        const [selectedDatasetsToOnboard, setSelectedDatasetsToOnboard] =
            useState<Dataset[]>([]);
        const selectedDatasetsToOnboardNames = useMemo(() => {
            return selectedDatasetsToOnboard.map((dataset) => dataset.name);
        }, [selectedDatasetsToOnboard]);

        const [tablesAlreadyOnboarded, tablesNotOnboarded] = useMemo(() => {
            let notOnboarded: Dataset[] = [];
            let onboarded: Dataset[] = [];

            if (existingDatasets && tables && selectedDataSource) {
                const onboardedNames = existingDatasets.map((t) => t.name);

                onboarded = existingDatasets;
                notOnboarded = tables.filter(
                    (t) => !onboardedNames.includes(t.name)
                );
            }

            return [onboarded, notOnboarded];
        }, [selectedDataSource, existingDatasets, tables]);

        useEffect(() => {
            if (selectedDataSource) {
                getTableForDatasourceID(selectedDataSource.id);
            }
        }, [selectedDataSource]);

        useEffect(() => {
            notifyIfErrors(
                getTablesStatus,
                getTableForDatasourceNameErrors,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.alert-templates"),
                })
            );
        }, [getTablesStatus, getTableForDatasourceNameErrors]);

        const handleToggleCheckbox = useCallback(
            (
                event: React.ChangeEvent<HTMLInputElement>,
                changedDataset: Dataset
            ): void => {
                const { checked } = event.target;

                if (!changedDataset) {
                    return;
                }

                setSelectedDatasetsToOnboard((currentlySelected) => {
                    if (!checked) {
                        return currentlySelected.filter(
                            (dataset) => dataset.name !== changedDataset.name
                        );
                    }

                    return [...currentlySelected, changedDataset];
                });
            },
            []
        );

        return (
            <>
                <PageContentsGridV1>
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <Grid container>
                                <InputSection
                                    helperLabel={
                                        selectedDataSource && (
                                            <DatasourceVerification
                                                datasourceId={
                                                    selectedDataSource.id
                                                }
                                            />
                                        )
                                    }
                                    inputComponent={
                                        <Autocomplete
                                            fullWidth
                                            getOptionLabel={(option) =>
                                                option.name as string
                                            }
                                            options={datasources}
                                            renderInput={(params) => (
                                                <TextField
                                                    {...params}
                                                    InputProps={{
                                                        ...params.InputProps,
                                                    }}
                                                    data-testId={
                                                        DATASET_FORM_TEST_IDS.DATASOURCE_AUTOCOMPLETE_TEXT_BOX
                                                    }
                                                    placeholder={t(
                                                        "label.select-a-datasource"
                                                    )}
                                                    variant="outlined"
                                                />
                                            )}
                                            value={selectedDataSource}
                                            onChange={(_, selected) => {
                                                setSelectedDataSource(
                                                    selected as Datasource
                                                );
                                            }}
                                        />
                                    }
                                    label={t("label.datasource")}
                                />

                                {!isEmpty(tablesAlreadyOnboarded) && (
                                    <>
                                        <Grid xs={12}>
                                            <Box p={1}>
                                                <Divider />
                                            </Box>
                                        </Grid>
                                        <InputSection
                                            inputComponent={
                                                <Grid container>
                                                    {tablesAlreadyOnboarded.map(
                                                        (dataset) => {
                                                            return (
                                                                <Grid
                                                                    item
                                                                    key={
                                                                        dataset.name
                                                                    }
                                                                    xs={12}
                                                                >
                                                                    {
                                                                        dataset.name
                                                                    }
                                                                </Grid>
                                                            );
                                                        }
                                                    )}
                                                </Grid>
                                            }
                                            label="Already onboarded"
                                        />
                                    </>
                                )}

                                {!isEmpty(tablesNotOnboarded) && (
                                    <>
                                        <Grid xs={12}>
                                            <Box p={1}>
                                                <Divider />
                                            </Box>
                                        </Grid>

                                        <InputSection
                                            inputComponent={
                                                <Grid container>
                                                    {tablesNotOnboarded.map(
                                                        (dataset) => {
                                                            return (
                                                                <Grid
                                                                    item
                                                                    key={
                                                                        dataset.name
                                                                    }
                                                                    xs={12}
                                                                >
                                                                    <SelectDatasetOption
                                                                        checked={selectedDatasetsToOnboardNames.includes(
                                                                            dataset.name
                                                                        )}
                                                                        key={
                                                                            dataset.name
                                                                        }
                                                                        labelPrimaryText={
                                                                            dataset.name
                                                                        }
                                                                        labelSecondaryText={t(
                                                                            "label.num-dimensions",
                                                                            {
                                                                                num: dataset
                                                                                    .dimensions
                                                                                    .length,
                                                                            }
                                                                        )}
                                                                        name={
                                                                            dataset.name
                                                                        }
                                                                        onChange={(
                                                                            e
                                                                        ) =>
                                                                            handleToggleCheckbox(
                                                                                e,
                                                                                dataset
                                                                            )
                                                                        }
                                                                    />
                                                                </Grid>
                                                            );
                                                        }
                                                    )}
                                                </Grid>
                                            }
                                            label="Select tables to onboard"
                                        />
                                    </>
                                )}
                            </Grid>

                            {!selectedDataSource && (
                                <Grid item xs={12}>
                                    <InputSection
                                        inputComponent={
                                            <Box p={3} textAlign="center">
                                                {t(
                                                    "message.select-a-datasource-to-view-available-datasets-to"
                                                )}
                                            </Box>
                                        }
                                    />
                                </Grid>
                            )}
                        </PageContentsCardV1>
                    </Grid>
                </PageContentsGridV1>

                <WizardBottomBar
                    handleNextClick={() => {
                        onSubmit &&
                            selectedDataSource &&
                            onSubmit(
                                selectedDatasetsToOnboard,
                                selectedDataSource.id
                            );
                    }}
                    nextButtonLabel={t("label.submit")}
                />
            </>
        );
    };
