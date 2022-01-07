import { Grid } from "@material-ui/core";
import { PageContentsGridV1, PageV1 } from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { DatasetCard } from "../../components/entity-cards/dataset-card/dataset-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { deleteDataset, getDataset } from "../../rest/datasets/datasets.rest";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { getUiDataset } from "../../utils/datasets/datasets.util";
import { isValidNumberId } from "../../utils/params/params.util";
import {
    getDatasetsAllPath,
    getDatasetsUpdatePath,
} from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { DatasetsViewPageParams } from "./dataset-view-page.interfaces";

export const DatasetsViewPage: FunctionComponent = () => {
    const [uiDataset, setUiDataset] = useState<UiDataset | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<DatasetsViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch dataset
        fetchDataset();
    }, [timeRangeDuration]);

    const fetchDataset = (): void => {
        setUiDataset(null);
        let fetchedUiDataset = {} as UiDataset;

        if (!isValidNumberId(params.id)) {
            // Invalid id
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.dataset"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            setUiDataset(fetchedUiDataset);

            return;
        }

        getDataset(toNumber(params.id))
            .then((dataset) => {
                fetchedUiDataset = getUiDataset(dataset);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                )
            )
            .finally(() => setUiDataset(fetchedUiDataset));
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
            .then(() => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.dataset") }),
                    getSuccessSnackbarOption()
                );

                // Redirect to datasets all path
                history.push(getDatasetsAllPath());
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.dataset") }),
                    getErrorSnackbarOption()
                )
            );
    };

    const handleDatasetEdit = (id: number): void => {
        history.push(getDatasetsUpdatePath(id));
    };

    return (
        <PageV1>
            <PageHeader title={uiDataset ? uiDataset.name : ""} />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {/* Dataset */}
                    <DatasetCard
                        uiDataset={uiDataset}
                        onDelete={handleDatasetDelete}
                        onEdit={handleDatasetEdit}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
