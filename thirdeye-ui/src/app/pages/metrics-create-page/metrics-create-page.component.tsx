import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { MetricsWizard } from "../../components/metrics-wizard/metrics-wizard.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { getAllDatasets } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { LogicalMetric } from "../../rest/dto/metric.interfaces";
import { createMetric } from "../../rest/metrics/metrics.rest";
import { getMetricsViewPath } from "../../utils/routes/routes.util";
import { getSuccessSnackbarOption } from "../../utils/snackbar/snackbar.util";

export const MetricsCreatePage: FunctionComponent = () => {
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const [loading, setLoading] = useState(true);
    const [datasets, setDatasets] = useState<Dataset[]>([]);
    const { enqueueSnackbar } = useSnackbar();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllDatasets();
    }, []);

    const onCreateMetricWizardFinish = (metric: LogicalMetric): void => {
        if (!metric) {
            return;
        }

        createMetric(metric).then((metric: LogicalMetric): void => {
            enqueueSnackbar(
                t("message.create-success", {
                    entity: t("label.metric"),
                }),
                getSuccessSnackbarOption()
            );

            // Redirect to metrics detail path
            history.push(getMetricsViewPath(metric?.id || 0));
        });
    };

    const fetchAllDatasets = (): void => {
        getAllDatasets()
            .then((alerts: Dataset[]): void => {
                setDatasets(alerts);
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
            <MetricsWizard
                datasets={datasets}
                onFinish={onCreateMetricWizardFinish}
            />
        </PageContents>
    );
};
