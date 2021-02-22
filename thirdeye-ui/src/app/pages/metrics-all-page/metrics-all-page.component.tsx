import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { MetricsList } from "../../components/metrics-list/metrics-list.component";
import { MetricsListData } from "../../components/metrics-list/metrics-list.interfaces";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { Metric } from "../../rest/dto/metric.interfaces";
import { deleteMetric, getAllMetrics } from "../../rest/metrics/metrics.rest";
import { getMetricTableDatas } from "../../utils/metrics-util/metrics-util";
import { getMetricsAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const MetricsAllPage: FunctionComponent = () => {
    const [metricsTableDatas, setMetricsTableDatas] = useState<
        Array<MetricsListData>
    >([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();
    const { showDialog } = useDialog();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                onClick: (): void => {
                    history.push(getMetricsAllPath());
                },
            },
        ]);

        fetchData();
    }, []);

    const onDeleteMetric = (metricsListData: MetricsListData): void => {
        if (!metricsListData) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: metricsListData.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteMetricConfirmation(metricsListData);
            },
        });
    };

    const onDeleteMetricConfirmation = (
        metricsListData: MetricsListData
    ): void => {
        if (!metricsListData) {
            return;
        }
        deleteMetric(metricsListData.id)
            .then((metric: Metric): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.metrics"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted Metric from fetched metrics
                removeMetricsCardData(metric);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.metrics"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchData = async (): Promise<void> => {
        try {
            const data = await getAllMetrics();
            setMetricsTableDatas(getMetricTableDatas(data));
        } catch (error) {
            console.log(error);
        }
    };

    const removeMetricsCardData = (metric: Metric): void => {
        if (!metric) {
            return;
        }

        setMetricsTableDatas(
            (metricListDatas) =>
                metricListDatas &&
                metricListDatas.filter(
                    (metricListData: MetricsListData): boolean => {
                        return metricListData.id !== metric.id;
                    }
                )
        );
    };

    return (
        <PageContents centered title={t("label.metrics")}>
            <MetricsList
                metrics={metricsTableDatas}
                onDelete={onDeleteMetric}
            />
        </PageContents>
    );
};
