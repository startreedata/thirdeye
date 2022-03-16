import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { DatasetListV1 } from "../../components/dataset-list-v1/dataset-list-v1.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import {
    deleteDataset,
    getAllDatasets,
} from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { getUiDatasets } from "../../utils/datasets/datasets.util";
import { getErrorMessage } from "../../utils/rest/rest.util";

export const DatasetsAllPage: FunctionComponent = () => {
    const [uiDatasets, setUiDatasets] = useState<UiDataset[] | null>(null);
    const { showDialog } = useDialog();
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
                const errMessage = getErrorMessage(error);

                notify(
                    NotificationTypeV1.Error,
                    errMessage || t("message.fetch-error")
                );
            })
            .finally(() => setUiDatasets(fetchedUiDatasets));
    };

    const handleDatasetDelete = (uiDataset: UiDataset): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiDataset.name }),
            okButtonLabel: t("label.delete"),
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
                const errMessage = getErrorMessage(error);

                notify(
                    NotificationTypeV1.Error,
                    errMessage ||
                        t("message.delete-error", {
                            entity: t("label.dataset"),
                        })
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
