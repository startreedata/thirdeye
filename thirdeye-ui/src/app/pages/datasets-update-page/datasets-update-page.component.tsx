import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { getDataset, updateDataset } from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";
import { DatasetsUpdatePageParams } from "./datasets-update-page.interfaces";

export const DatasetsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [dataset, setDataset] = useState<Dataset>();
    const [datasources, setDatasources] = useState<Datasource[]>([]);
    const params = useParams<DatasetsUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

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
                navigate(getDatasetsViewPath(dataset.id));
            })
            .catch((error: AxiosError): void => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.update-error", {
                              entity: t("label.dataset"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    const fetchDataset = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
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
                    const axiosError =
                        datasourcesResponse.status === "rejected"
                            ? datasourcesResponse.reason
                            : datasetResponse.status === "rejected"
                            ? datasetResponse.reason
                            : ({} as AxiosError);
                    const errMessages = getErrorMessages(axiosError);
                    isEmpty(errMessages)
                        ? notify(
                              NotificationTypeV1.Error,
                              t("message.fetch-error")
                          )
                        : errMessages.map((err) =>
                              notify(NotificationTypeV1.Error, err)
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
