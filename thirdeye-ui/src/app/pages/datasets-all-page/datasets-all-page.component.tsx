/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { DatasetListV1 } from "../../components/dataset-list-v1/dataset-list-v1.component";
import {
    deleteDataset,
    getAllDatasets,
} from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { getUiDatasets } from "../../utils/datasets/datasets.util";
import { getErrorMessages } from "../../utils/rest/rest.util";

export const DatasetsAllPage: FunctionComponent = () => {
    const [uiDatasets, setUiDatasets] = useState<UiDataset[] | null>(null);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch datasets
        fetchAllDatasets();
    }, []);

    const fetchAllDatasets = (): void => {
        setUiDatasets(null);

        let fetchedUiDatasets: UiDataset[] = [];
        getAllDatasets()
            .then((datasets) => {
                fetchedUiDatasets = getUiDatasets(datasets);
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.error-while-fetching", {
                              entity: t("label.datasets"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            })
            .finally(() => setUiDatasets(fetchedUiDatasets));
    };

    const handleDatasetDelete = (uiDataset: UiDataset): void => {
        showDialog({
            contents: t("message.delete-confirmation", {
                name: uiDataset.name,
            }),
            okButtonText: t("label.delete"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleDatasetDeleteOk(uiDataset),
        });
    };

    const handleDatasetDeleteOk = (uiDataset: UiDataset): void => {
        deleteDataset(uiDataset.id)
            .then((dataset) => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", { entity: t("label.dataset") })
                );

                // Remove deleted dataset from fetched datasets
                removeUiDataset(dataset);
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.delete-error", {
                              entity: t("label.dataset"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    const removeUiDataset = (dataset: Dataset): void => {
        if (!dataset) {
            return;
        }

        setUiDatasets(
            (uiDatasets) =>
                uiDatasets &&
                uiDatasets.filter((uiDataset) => uiDataset.id !== dataset.id)
        );
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={1} />
            <PageContentsGridV1 fullHeight>
                <DatasetListV1
                    datasets={uiDatasets}
                    onDelete={handleDatasetDelete}
                />
            </PageContentsGridV1>
        </PageV1>
    );
};
