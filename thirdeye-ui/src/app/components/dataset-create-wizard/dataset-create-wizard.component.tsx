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
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
    AppLoadingIndicatorV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDatasets } from "../../rest/datasets/datasets.actions";
import { useGetDatasources } from "../../rest/datasources/datasources.actions";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { LoadingErrorStateSwitch } from "../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { DatasetCreateWizardProps } from "./dataset-create-wizard.interfaces";
import { DatasetPropertiesForm } from "./dataset-properties-form/dataset-properties-form.component";

export const DatasetCreateWizard: FunctionComponent<DatasetCreateWizardProps> =
    ({ onSubmit }) => {
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();

        const { datasources, getDatasources, status, errorMessages } =
            useGetDatasources();
        const {
            datasets,
            getDatasets,
            status: getDatasetsStatus,
        } = useGetDatasets();

        useEffect(() => {
            getDatasets();
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
            <LoadingErrorStateSwitch
                isError={false}
                isLoading={
                    status === ActionStatus.Working ||
                    status === ActionStatus.Initial ||
                    getDatasetsStatus === ActionStatus.Working ||
                    getDatasetsStatus === ActionStatus.Initial
                }
                loadingState={<AppLoadingIndicatorV1 />}
            >
                <DatasetPropertiesForm
                    datasources={datasources || []}
                    existingDatasets={datasets || []}
                    onSubmit={onSubmit}
                />
            </LoadingErrorStateSwitch>
        );
    };
