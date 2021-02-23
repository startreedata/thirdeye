import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { MetricCardData } from "../../components/entity-cards/metric-card/metric-card.interfaces";
import { MetricsList } from "../../components/metrics-list/metrics-list.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { Metric } from "../../rest/dto/metric.interfaces";
import { deleteMetric, getAllMetrics } from "../../rest/metrics/metrics.rest";
import { getMetricCardDatas } from "../../utils/metrics/metrics.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const MetricsAllPage: FunctionComponent = () => {
    const [metricCardDatas, setMetricCardDatas] = useState<
        MetricCardData[] | null
    >(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllMetrics();
    }, []);

    const onDeleteMetric = (metricCardData: MetricCardData): void => {
        if (!metricCardData) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: metricCardData.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteMetricConfirmation(metricCardData);
            },
        });
    };

    const onDeleteMetricConfirmation = (
        metricCardData: MetricCardData
    ): void => {
        if (!metricCardData) {
            return;
        }

        deleteMetric(metricCardData.id)
            .then((metric: Metric): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.metric"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted metric from fetched metrics
                removeMetricCardData(metric);
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
        setMetricCardDatas(null);
        let fetchedMetricCardDatas: MetricCardData[] = [];
        getAllMetrics()
            .then((metrics: Metric[]): void => {
                fetchedMetricCardDatas = getMetricCardDatas(metrics);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setMetricCardDatas(fetchedMetricCardDatas);
            });
    };

    const removeMetricCardData = (metric: Metric): void => {
        if (!metric) {
            return;
        }

        setMetricCardDatas(
            (metricCardDatas) =>
                metricCardDatas &&
                metricCardDatas.filter(
                    (metricCardData: MetricCardData): boolean => {
                        return metricCardData.id !== metric.id;
                    }
                )
        );
    };

    return (
        <PageContents
            centered
            hideTimeRange
            maxRouterBreadcrumbs={1}
            title={t("label.metrics")}
        >
            <MetricsList
                metricCardDatas={metricCardDatas}
                onDelete={onDeleteMetric}
            />
        </PageContents>
    );
};
