import { Grid } from "@material-ui/core";
import { cloneDeep, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasetDatasourcesAccordian } from "../../components/dataset-datasources-accordian/dataset-datasources-accordian.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { DatasetCard } from "../../components/entity-cards/dataset-card/dataset-card.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteDataset,
    getDataset,
    updateDataset,
} from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { getUiDataset } from "../../utils/datasets/datasets.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getDatasetsAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { DatasetsViewPageParams } from "./dataset-view-page.interfaces";

export const DatasetsViewPage: FunctionComponent = () => {
    const [uiDataset, setUiDataset] = useState<UiDataset | null>(null);
    const [datasources, setDatasources] = useState<Datasource[]>([]);
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
        let fetchedDatasources: Datasource[] = [];

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
            setDatasources(fetchedDatasources);

            return;
        }

        Promise.allSettled([
            getDataset(toNumber(params.id)),
            getAllDatasources(),
        ])
            .then(([datasetResponse, datasourcesResponse]) => {
                // Determine if any of the calls failed
                if (
                    datasetResponse.status === "rejected" ||
                    datasourcesResponse.status === "rejected"
                ) {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );
                }

                // Attempt to gather data
                if (datasourcesResponse.status === "fulfilled") {
                    fetchedDatasources = datasourcesResponse.value;
                }
                if (datasetResponse.status === "fulfilled") {
                    fetchedUiDataset = getUiDataset(
                        datasetResponse.value,
                        fetchedDatasources
                    );
                }
            })
            .finally(() => {
                setUiDataset(fetchedUiDataset);
                setDatasources(fetchedDatasources);
            });
    };

    const handleDatasetDelete = (uiDataset: UiDataset): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiDataset.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleDatasetDeleteOk(uiDataset),
        });
    };

    const handleDatasetDeleteOk = (uiDataset: UiDataset): void => {
        deleteDataset(uiDataset.id)
            .then(() => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.dataset"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to datasets all path
                history.push(getDatasetsAllPath());
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.dataset"),
                    }),
                    getErrorSnackbarOption()
                )
            );
    };

    const handleDatasetDatasourcesChange = (
        datasources: Datasource[]
    ): void => {
        if (!uiDataset || !uiDataset.dataset) {
            return;
        }

        // Create a copy of dataset and update datasources
        const datasetCopy = cloneDeep(uiDataset.dataset);
        datasetCopy.datasources = datasources;
        saveDataset(datasetCopy);
    };

    const saveDataset = (dataset: Dataset): void => {
        updateDataset(dataset)
            .then((dataset) => {
                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.dataset"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Replace updated dataset as fetched dataset
                setUiDataset(getUiDataset(dataset, datasources));
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.update-error", {
                        entity: t("label.dataset"),
                    }),
                    getErrorSnackbarOption()
                )
            );
    };

    return (
        <PageContents
            centered
            hideTimeRange
            title={uiDataset ? uiDataset.name : ""}
        >
            <Grid container>
                {/* Dataset */}
                <Grid item xs={12}>
                    <DatasetCard
                        uiDataset={uiDataset}
                        onDelete={handleDatasetDelete}
                    />
                </Grid>

                {/* Associated datasources */}
                <Grid item xs={12}>
                    <DatasetDatasourcesAccordian
                        dataset={uiDataset}
                        datasources={datasources}
                        title={t("label.associated-datasources")}
                        onChange={handleDatasetDatasourcesChange}
                    />
                </Grid>
            </Grid>
        </PageContents>
    );
};
