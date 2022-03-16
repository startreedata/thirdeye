import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { onBoardDataset } from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { getErrorMessage } from "../../utils/rest/rest.util";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";

export const DatasetsOnboardPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasources, setDatasources] = useState<Datasource[]>([]);
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchAllDatasources();
    }, []);

    const onDatasetWizardFinish = (dataset: Dataset): void => {
        if (!dataset) {
            return;
        }

        onBoardDataset(dataset.name, dataset.dataSource.name)
            .then((dataset: Dataset): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.onboard-success", {
                        entity: t("label.dataset"),
                    })
                );

                // Redirect to datasets detail path
                navigate(getDatasetsViewPath(dataset.id));
            })
            .catch((error: AxiosError): void => {
                const errMessage = getErrorMessage(error);

                notify(
                    NotificationTypeV1.Error,
                    errMessage ||
                        t("message.onboard-error", {
                            entity: t("label.dataset"),
                        })
                );
            });
    };

    const fetchAllDatasources = (): void => {
        getAllDatasources()
            .then((datasources: Datasource[]): void => {
                setDatasources(datasources);
            })
            .catch((error: AxiosError): void => {
                const errMessage = getErrorMessage(error);

                notify(
                    NotificationTypeV1.Error,
                    errMessage || t("message.fetch-error")
                );
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
                title={t("label.onboard-entity", {
                    entity: t("label.dataset"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <DatasetWizard
                        datasources={datasources}
                        onFinish={onDatasetWizardFinish}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
