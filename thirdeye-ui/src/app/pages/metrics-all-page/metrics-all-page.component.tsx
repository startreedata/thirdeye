import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { MetricList } from "../../components/metric-list/metric-list.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { Metric } from "../../rest/dto/metric.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getAllMetrics } from "../../rest/metrics/metrics.rest";
import { getUiMetrics } from "../../utils/metrics/metrics.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const MetricsAllPage: FunctionComponent = () => {
    const [uiMetrics, setUiMetrics] = useState<UiMetric[] | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllMetrics();
    }, []);

    const onDeleteMetric = (uiMetric: UiMetric): void => {
        if (!uiMetric) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiMetric.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteMetricConfirmation(uiMetric);
            },
        });
    };

    const onDeleteMetricConfirmation = (uiMetric: UiMetric): void => {
        if (!uiMetric) {
            return;
        }

        deleteMetric(uiMetric.id)
            .then((metric: Metric): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.metric"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted metric from fetched metrics
                removeUiMetric(metric);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.metric"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAllMetrics = (): void => {
        setUiMetrics(null);
        let fetchedUiMetrics: UiMetric[] = [];
        getAllMetrics()
            .then((metrics: Metric[]): void => {
                fetchedUiMetrics = getUiMetrics(metrics);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setUiMetrics(fetchedUiMetrics);
            });
    };

    const removeUiMetric = (metric: Metric): void => {
        if (!metric) {
            return;
        }

        setUiMetrics(
            (uiMetrics) =>
                uiMetrics &&
                uiMetrics.filter((uiMetric: UiMetric): boolean => {
                    return uiMetric.id !== metric.id;
                })
        );
    };

    return (
        <PageContents
            centered
            hideTimeRange
            maxRouterBreadcrumbs={1}
            title={t("label.metrics")}
        >
            <MetricList uiMetrics={uiMetrics} onDelete={onDeleteMetric} />
        </PageContents>
    );
};
