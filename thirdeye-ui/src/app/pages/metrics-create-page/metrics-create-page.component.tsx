/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { MetricsWizard } from "../../components/metrics-wizard/metrics-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { getAllDatasets } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { LogicalMetric } from "../../rest/dto/metric.interfaces";
import { createMetric } from "../../rest/metrics/metrics.rest";
import { getMetricsViewPath } from "../../utils/routes/routes.util";

export const MetricsCreatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasets, setDatasets] = useState<Dataset[]>([]);
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchAllDatasets();
    }, []);

    const onCreateMetricWizardFinish = (metric: LogicalMetric): void => {
        if (!metric) {
            return;
        }

        createMetric(metric).then((metric: LogicalMetric): void => {
            notify(
                NotificationTypeV1.Success,
                t("message.create-success", {
                    entity: t("label.metric"),
                })
            );

            // Redirect to metrics detail path
            navigate(getMetricsViewPath(metric?.id || 0));
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
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.metric"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <MetricsWizard
                        datasets={datasets}
                        onFinish={onCreateMetricWizardFinish}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
