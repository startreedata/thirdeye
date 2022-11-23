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
    FormControl,
    FormControlLabel,
    FormLabel,
    Radio,
    RadioGroup,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    JSONEditorV1,
    PageContentsCardV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDatasources } from "../../rest/datasources/datasources.actions";
import type { Datasource } from "../../rest/dto/datasource.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import type {
    DatasourceOptionGroups,
    SelectedDatasource,
    WelcomeSelectDatasourceOutletContext,
} from "./welcome-onboard-datasource-select-datasource.interfaces";
import {
    ADD_NEW_DATASOURCE,
    getDatasourceGroups,
} from "./welcome-onboard-datasource-select-datasource.utils";

export const WelcomeSelectDatasource: FunctionComponent = () => {
    const {
        editedDatasource,
        setEditedDatasource,
        selectedDatasourceName,
        setSelectedDatasourceName,
    } = useOutletContext<WelcomeSelectDatasourceOutletContext>();

    const handleDatasourceChange = (value: string): void => {
        setEditedDatasource(JSON.parse(value));
    };
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    const handleRadioChange = (
        _e: React.ChangeEvent<HTMLInputElement>,
        value: string
    ): void => {
        setSelectedDatasourceName(value as SelectedDatasource);
    };

    const { datasources, getDatasources, status, errorMessages } =
        useGetDatasources();

    const datasourceGroups = useMemo<DatasourceOptionGroups[]>(
        () => getDatasourceGroups(datasources || []),
        [datasources]
    );

    useEffect(() => {
        getDatasources();
    }, []);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasources"),
            })
        );
    }, [status]);

    return (
        <PageContentsCardV1>
            <Box px={2} py={2}>
                <Typography variant="h5">Select datasource</Typography>
                <Typography variant="body2">
                    You can always add, remove or change datasources in the
                    configuration section.
                </Typography>
            </Box>

            <LoadingErrorStateSwitch
                isError={status === ActionStatus.Error}
                isLoading={status === ActionStatus.Working}
            >
                {datasourceGroups.map((datasourceGroup) => (
                    <Box key={datasourceGroup.key} px={2} py={1}>
                        <FormControl component="fieldset">
                            <FormLabel color="secondary" component="legend">
                                {datasourceGroup.title}
                            </FormLabel>
                            <RadioGroup
                                aria-label="Select Datasource"
                                name="select-datasource"
                                value={selectedDatasourceName}
                                onChange={handleRadioChange}
                            >
                                {datasourceGroup.options.map(
                                    (datasourceOption) => (
                                        <FormControlLabel
                                            control={<Radio />}
                                            key={datasourceOption.value}
                                            label={datasourceOption.label}
                                            value={datasourceOption.value.toString()}
                                        />
                                    )
                                )}
                            </RadioGroup>
                        </FormControl>
                    </Box>
                ))}
            </LoadingErrorStateSwitch>

            {selectedDatasourceName === ADD_NEW_DATASOURCE ? (
                <JSONEditorV1<Datasource>
                    hideValidationSuccessIcon
                    value={editedDatasource}
                    onChange={handleDatasourceChange}
                />
            ) : null}
        </PageContentsCardV1>
    );
};
