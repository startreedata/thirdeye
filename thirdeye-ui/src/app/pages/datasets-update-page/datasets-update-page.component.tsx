import { Grid } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
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
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { getDataset, updateDataset } from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { isValidNumberId } from "../../utils/params/params.util";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";
import { DatasetsUpdatePageParams } from "./datasets-update-page.interfaces";

export const DatasetsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [dataset, setDataset] = useState<Dataset>();
    const [datasources, setDatasources] = useState<Datasource[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const params = useParams<DatasetsUpdatePageParams>();
    const history = useHistory();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

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
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.dataset"),
                    })
                );

                // Redirect to datasets detail path
                history.push(getDatasetsViewPath(dataset.id));
            })
            .catch((): void => {
                notify(
                    NotificationTypeV1.Error,
                    t("message.update-error", {
                        entity: t("label.dataset"),
                    })
                );
            });
    };

    const fetchDataset = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.dataset"),
                    id: params.id,
                })
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
                    notify(NotificationTypeV1.Error, t("message.fetch-error"));
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
        <PageV1>
            <PageHeader
                title={t("label.update-entity", {
                    entity: t("label.dataset"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {dataset && (
                        <DatasetWizard
                            dataset={dataset}
                            datasources={datasources}
                            onFinish={onDatasetWizardFinish}
                        />
                    )}

                    {/* No data available message */}
                    {!dataset && <NoDataIndicator />}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
