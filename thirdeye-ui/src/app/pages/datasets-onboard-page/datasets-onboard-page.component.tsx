import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { onBoardDataset } from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const DatasetsOnboardPage: FunctionComponent = () => {
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

        onBoardDataset(dataset.name, dataset.dataSource.name)
            .then((dataset: Dataset): void => {
                enqueueSnackbar(
                    t("message.onboard-success", {
                        entity: t("label.dataset"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to datasets detail path
                history.push(getDatasetsViewPath(dataset.id));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.onboard-error", {
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
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageContents centered hideTimeRange title={t("label.onboard")}>
            <DatasetWizard
                datasources={datasources}
                onFinish={onDatasetWizardFinish}
            />
        </PageContents>
    );
};
