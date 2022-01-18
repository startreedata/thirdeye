import { Grid } from "@material-ui/core";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
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
import { DatasetsViewPageParams } from "./dataset-view-page.interfaces";

export const DatasetsViewPage: FunctionComponent = () => {
    const [uiDataset, setUiDataset] = useState<UiDataset | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const params = useParams<DatasetsViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

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
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.dataset"),
                    id: params.id,
                })
            );

            setUiDataset(fetchedUiDataset);

            return;
        }

        getDataset(toNumber(params.id))
            .then((dataset) => {
                fetchedUiDataset = getUiDataset(dataset);
            })
            .catch(() =>
                notify(NotificationTypeV1.Error, t("message.fetch-error"))
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
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", { entity: t("label.dataset") })
                );

                // Redirect to datasets all path
                history.push(getDatasetsAllPath());
            })
            .catch(() =>
                notify(
                    NotificationTypeV1.Error,
                    t("message.delete-error", { entity: t("label.dataset") })
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
