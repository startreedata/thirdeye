import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasetList } from "../../components/dataset-list/dataset-list.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteDataset,
    getAllDatasets,
} from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { getUiDatasets } from "../../utils/datasets/datasets.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const DatasetsAllPage: FunctionComponent = () => {
    const [uiDatasets, setUiDatasets] = useState<UiDataset[] | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch datasets
        fetchAllDatasets();
    }, [timeRangeDuration]);

    const fetchAllDatasets = (): void => {
        setUiDatasets(null);

        let fetchedUiDatasets: UiDataset[] = [];
        getAllDatasets()
            .then((datasets) => {
                fetchedUiDatasets = getUiDatasets(datasets);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                )
            )
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
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.dataset") }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted dataset from fetched datasets
                removeUiDataset(dataset);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.dataset") }),
                    getErrorSnackbarOption()
                )
            );
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
        <PageContents
            centered
            hideTimeRange
            maxRouterBreadcrumbs={1}
            title={t("label.datasets")}
        >
            {/* dataset list */}
            <DatasetList datasets={uiDatasets} onDelete={handleDatasetDelete} />
        </PageContents>
    );
};
