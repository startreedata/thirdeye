import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { MetricListV1 } from "../../components/metric-list-v1/metric-list-v1.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { Metric } from "../../rest/dto/metric.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getAllMetrics } from "../../rest/metrics/metrics.rest";
import { getUiMetrics } from "../../utils/metrics/metrics.util";

export const MetricsAllPage: FunctionComponent = () => {
    const [uiMetrics, setUiMetrics] = useState<UiMetric[] | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch metrics
        fetchAllMetrics();
    }, [timeRangeDuration]);

    const fetchAllMetrics = (): void => {
        setUiMetrics(null);

        let fetchedUiMetrics: UiMetric[] = [];
        getAllMetrics()
            .then((metrics) => {
                fetchedUiMetrics = getUiMetrics(metrics);
            })
            .finally(() => setUiMetrics(fetchedUiMetrics));
    };

    const handleMetricDelete = (uiMetric: UiMetric): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiMetric.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleMetricDeleteOk(uiMetric),
        });
    };

    const handleMetricDeleteOk = (uiMetric: UiMetric): void => {
        deleteMetric(uiMetric.id).then((metric) => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.metric") })
            );

            // Remove deleted metric from fetched metrics
            removeUiMetric(metric);
        });
    };

    const removeUiMetric = (metric: Metric): void => {
        if (!metric) {
            return;
        }

        setUiMetrics(
            (uiMetrics) =>
                uiMetrics &&
                uiMetrics.filter((uiMetric) => uiMetric.id !== metric.id)
        );
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={3} />
            <PageContentsGridV1 fullHeight>
                <MetricListV1
                    metrics={uiMetrics}
                    onDelete={handleMetricDelete}
                />
            </PageContentsGridV1>
        </PageV1>
    );
};
