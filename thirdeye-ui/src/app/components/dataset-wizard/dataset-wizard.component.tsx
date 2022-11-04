// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    AppLoadingIndicatorV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { useGetDatasources } from "../../rest/datasources/datasources.actions";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { LoadingErrorStateSwitch } from "../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { DatasetPropertiesForm } from "./dataset-properties-form/dataset-properties-form.component";
import { DatasetWizardProps } from "./dataset-wizard.interfaces";

const FORM_ID_DATASET_PROPERTIES = "FORM_ID_DATASET_PROPERTIES";

export const DatasetWizard: FunctionComponent<DatasetWizardProps> = ({
    dataset,
    onCancel,
    onSubmit,
    submitBtnLabel,
}) => {
    const [loading, setLoading] = useState(true);
    const [modifiedDataset] = useState<Dataset>(dataset);
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const { datasources, getDatasources, status, errorMessages } =
        useGetDatasources();

    useEffect(() => {
        getDatasources().finally(() => {
            setLoading(false);
        });
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
            isLoading={loading}
            loadingState={<AppLoadingIndicatorV1 />}
        >
            <DatasetPropertiesForm
                dataset={modifiedDataset}
                datasources={datasources || []}
                id={FORM_ID_DATASET_PROPERTIES}
                submitBtnLabel={submitBtnLabel}
                onCancel={onCancel}
                onSubmit={onSubmit}
            />
        </LoadingErrorStateSwitch>
    );
};
