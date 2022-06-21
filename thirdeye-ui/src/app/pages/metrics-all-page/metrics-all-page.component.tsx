/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { MetricListV1 } from "../../components/metric-list-v1/metric-list-v1.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { Metric } from "../../rest/dto/metric.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getAllMetrics } from "../../rest/metrics/metrics.rest";
import { getUiMetrics } from "../../utils/metrics/metrics.util";

export const MetricsAllPage: FunctionComponent = () => {
    const [uiMetrics, setUiMetrics] = useState<UiMetric[] | null>(null);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch metrics
        fetchAllMetrics();
    }, []);

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
            contents: t("message.delete-confirmation", {
                name: uiMetric.name,
            }),
            okButtonText: t("label.delete"),
            cancelButtonText: t("label.cancel"),
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
            <ConfigurationPageHeader selectedIndex={2} />
            <PageContentsGridV1 fullHeight>
                <MetricListV1
                    metrics={uiMetrics}
                    onDelete={handleMetricDelete}
                />
            </PageContentsGridV1>
        </PageV1>
    );
};
