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
import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { MetricCard } from "../../components/entity-cards/metric-card/metric-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getMetric } from "../../rest/metrics/metrics.rest";
import { getUiMetric } from "../../utils/metrics/metrics.util";
import { isValidNumberId } from "../../utils/params/params.util";
import {
    getMetricsAllPath,
    getMetricsUpdatePath,
} from "../../utils/routes/routes.util";
import { MetricsViewPageParams } from "./metrics-view-page.interfaces";

export const MetricsViewPage: FunctionComponent = () => {
    const [uiMetric, setUiMetric] = useState<UiMetric | null>(null);

    const { showDialog } = useDialogProviderV1();
    const params = useParams<MetricsViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch metric
        fetchMetric();
    }, []);

    const fetchMetric = (): void => {
        setUiMetric(null);
        let fetchedUiMetric = {} as UiMetric;

        if (params.id && !isValidNumberId(params.id)) {
            // Invalid id
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.metric"),
                    id: params.id,
                })
            );

            setUiMetric(fetchedUiMetric);

            return;
        }

        getMetric(toNumber(params.id))
            .then((metric) => {
                fetchedUiMetric = getUiMetric(metric);
            })
            .finally(() => setUiMetric(fetchedUiMetric));
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
        deleteMetric(uiMetric.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.metric") })
            );

            // Redirect to metrics all path
            navigate(getMetricsAllPath());
        });
    };

    const handleMetricEdit = (id: number): void => {
        navigate(getMetricsUpdatePath(id));
    };

    return (
        <PageV1>
            <PageHeader
                showCreateButton
                title={uiMetric ? uiMetric.name : ""}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {/* Metric */}
                    <MetricCard
                        metric={uiMetric}
                        onDelete={handleMetricDelete}
                        onEdit={handleMetricEdit}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
