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

import {
    Box,
    Checkbox,
    Divider,
    FormControl,
    FormControlLabel,
    FormGroup,
    FormHelperText,
    Typography,
} from "@material-ui/core";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useParams } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    PageContentsCardV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDatasets } from "../../rest/datasets/datasets.actions";
import { getDatasource } from "../../rest/datasources/datasources.rest";
import type { Datasource } from "../../rest/dto/datasource.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import type {
    SelectDatasetProps,
    WelcomeSelectDatasetOutletContext,
} from "./welcome-onboard-datasource-select-datasets.interfaces";
import { SELECT_ALL } from "./welcome-onboard-datasource-select-datasets.utils";

const SelectDataset: FunctionComponent<SelectDatasetProps> = ({
    checked,
    indeterminate,
    onChange,
    labelPrimaryText,
    labelSecondaryText,
    name,
}) => {
    return (
        <FormControl component="fieldset" margin="dense">
            <FormControlLabel
                control={
                    <Checkbox
                        checked={checked}
                        {...(indeterminate && { indeterminate })}
                        color="primary"
                        inputProps={{ "aria-label": name }}
                        name={name}
                        onChange={onChange}
                    />
                }
                label={
                    <>
                        {labelPrimaryText}
                        {labelSecondaryText ? (
                            <FormHelperText>
                                {labelSecondaryText}
                            </FormHelperText>
                        ) : null}
                    </>
                }
            />
        </FormControl>
    );
};

export const WelcomeSelectDatasets: FunctionComponent = () => {
    const queryParams: { id?: string } = useParams();
    const selectedDatasource = Number(queryParams.id);

    const { selectedDatasets, setSelectedDatasets } =
        useOutletContext<WelcomeSelectDatasetOutletContext>();

    const [datasource, setDatasource] = useState<Datasource | null>(null);
    const [datasourceStatus, setDatasourceStatus] = useState<ActionStatus>(
        ActionStatus.Initial
    );

    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    const handleToggleCheckbox = useCallback(
        (event: React.ChangeEvent<HTMLInputElement>): void => {
            const { name, checked } = event.target;
            if (!name) {
                return;
            }
            if (name === SELECT_ALL) {
                setSelectedDatasets((selectedStateProp) => {
                    const areAllCheckedState = !Object.values(
                        selectedStateProp
                    ).some((v) => !v);
                    const newState = Object.assign(
                        {},
                        ...Object.keys(selectedStateProp).map((k) => ({
                            [k]: !areAllCheckedState,
                        }))
                    );

                    return newState;
                });

                return;
            }

            setSelectedDatasets((selectedStateProp) => ({
                ...selectedStateProp,
                [name]: checked,
            }));
        },
        []
    );

    useEffect(() => {
        if (selectedDatasource) {
            setDatasourceStatus(ActionStatus.Working);
            getDatasource(Number(selectedDatasource))
                .then((datasourceProp: Datasource) => {
                    setDatasource(datasourceProp);
                    setDatasourceStatus(ActionStatus.Done);
                })
                .catch(() => {
                    setDatasourceStatus(ActionStatus.Error);
                });
        } else {
            setDatasourceStatus(ActionStatus.Error);
        }
    }, []);

    const {
        datasets,
        getDatasets,
        status: datasetsStatus,
        errorMessages,
    } = useGetDatasets();

    useEffect(() => {
        getDatasets().then((datasetsProp) => {
            if (!datasetsProp) {
                return;
            }
            setSelectedDatasets(
                Object.assign(
                    {},
                    ...datasetsProp?.map(({ id }) => ({ [id]: false }))
                )
            );
        });
    }, []);

    useEffect(() => {
        notifyIfErrors(
            datasetsStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasources"),
            })
        );
    }, [datasetsStatus]);

    const areSomeChecked = Object.values(selectedDatasets).some((v) => v);
    const areAllChecked = !Object.values(selectedDatasets).some((v) => !v);

    const selectAllProps = useMemo(
        () => ({
            checked: areAllChecked,
            indeterminate: areSomeChecked && !areAllChecked,
            labelPrimaryText: "Select All",
            name: SELECT_ALL,
            onChange: handleToggleCheckbox,
        }),
        [areSomeChecked, areAllChecked]
    );

    return (
        <LoadingErrorStateSwitch
            isError={datasourceStatus === ActionStatus.Error}
            isLoading={datasourceStatus === ActionStatus.Working}
        >
            <PageContentsCardV1>
                <Box px={2} py={2}>
                    <Typography variant="h5">
                        Onboard datasets for {datasource?.name}
                    </Typography>
                    <Typography variant="body2">
                        Select the datasets you want to include in your
                        configuration
                    </Typography>
                    <LoadingErrorStateSwitch
                        isError={datasetsStatus === ActionStatus.Error}
                        isLoading={datasetsStatus === ActionStatus.Working}
                    >
                        <Box alignItems="flexStart" display="flex" mt={2}>
                            <FormGroup>
                                <SelectDataset {...selectAllProps} />
                                <Divider />

                                {datasets?.map((dataset) => (
                                    <SelectDataset
                                        checked={
                                            !!selectedDatasets?.[dataset.id]
                                        }
                                        key={dataset.id}
                                        labelPrimaryText={dataset.name}
                                        labelSecondaryText={`${dataset.dimensions.length} dimensions`}
                                        name={`${dataset.id}`}
                                        onChange={handleToggleCheckbox}
                                    />
                                ))}
                            </FormGroup>
                        </Box>
                    </LoadingErrorStateSwitch>
                </Box>
            </PageContentsCardV1>
        </LoadingErrorStateSwitch>
    );
};
