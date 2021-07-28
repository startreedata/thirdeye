import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { createDataset } from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const DatasetsCreatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasources, setDatasources] = useState<Datasource[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllDatasources();
    }, []);

    const onDatasetWizardFinish = (dataset: Dataset): void => {
        if (!dataset) {
            return;
        }

        createDataset(dataset)
            .then((dataset: Dataset): void => {
                enqueueSnackbar(
                    t("message.create-success", {
                        entity: t("label.dataset"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to datasets detail path
                history.push(getDatasetsViewPath(dataset.id));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.create-error", {
                        entity: t("label.dataset"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAllDatasources = (): void => {
        getAllDatasources()
            .then((datasources: Datasource[]): void => {
                setDatasources(datasources);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents centered hideTimeRange title={t("label.create")}>
            <DatasetWizard
                datasources={datasources}
                onFinish={onDatasetWizardFinish}
            />
        </PageContents>
    );
};
