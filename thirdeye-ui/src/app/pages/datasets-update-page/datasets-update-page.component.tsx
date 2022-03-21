import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { getDataset, updateDataset } from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { isValidNumberId } from "../../utils/params/params.util";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { DatasetsUpdatePageParams } from "./datasets-update-page.interfaces";

export const DatasetsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [dataset, setDataset] = useState<Dataset>();
    const [datasources, setDatasources] = useState<Datasource[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<DatasetsUpdatePageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Fetched dataset changed, set breadcrumbs
        setPageBreadcrumbs([
            {
                text: dataset ? dataset.name : "",
                onClick: (): void => {
                    if (dataset) {
                        history.push(getDatasetsViewPath(dataset.id));
                    }
                },
            },
        ]);
    }, [dataset]);

    useEffect(() => {
        fetchDataset();
    }, []);

    const onDatasetWizardFinish = (dataset: Dataset): void => {
        if (!dataset) {
            return;
        }

        updateDataset(dataset)
            .then((dataset: Dataset): void => {
                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.dataset"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to datasets detail path
                history.push(getDatasetsViewPath(dataset.id));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", {
                        entity: t("label.dataset"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchDataset = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.dataset"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

            return;
        }

        Promise.allSettled([
            getDataset(toNumber(params.id)),
            getAllDatasources(),
        ])
            .then(([datasetResponse, datasourcesResponse]): void => {
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
                if (datasetResponse.status === "fulfilled") {
                    setDataset(datasetResponse.value);
                }
                if (datasourcesResponse.status === "fulfilled") {
                    setDatasources(datasourcesResponse.value);
                }
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageContents centered title={t("label.update")}>
            {dataset && (
                <DatasetWizard
                    dataset={dataset}
                    datasources={datasources}
                    onFinish={onDatasetWizardFinish}
                />
            )}

            {/* No data available message */}
            {!dataset && <NoDataIndicator />}
        </PageContents>
    );
};
