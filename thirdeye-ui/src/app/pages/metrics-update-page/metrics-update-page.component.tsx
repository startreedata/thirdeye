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
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { AxiosError } from "axios";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { MetricsWizard } from "../../components/metrics-wizard/metrics-wizard.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { getAllDatasets } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { LogicalMetric, Metric } from "../../rest/dto/metric.interfaces";
import { getMetric, updateMetric } from "../../rest/metrics/metrics.rest";
import { PROMISES } from "../../utils/constants/constants.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getMetricsViewPath } from "../../utils/routes/routes.util";
import { MetricsUpdatePageParams } from "./metrics-update-page.interfaces";

export const MetricsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [metric, setMetric] = useState<Metric>();
    const [datasets, setDatasets] = useState<Dataset[]>([]);

    const params = useParams<MetricsUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchMetric();
    }, []);

    const onMetricWizardFinish = (newMetric: LogicalMetric): void => {
        if (!metric) {
            return;
        }

        updateMetric(newMetric).then((newMetric: LogicalMetric): void => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.metric"),
                })
            );

            // Redirect to metric detail path
            navigate(getMetricsViewPath(newMetric.id || 0));
        });
    };

    const fetchMetric = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.metric"),
                    id: params.id,
                })
            );

            setLoading(false);

            return;
        }

        Promise.allSettled([getMetric(toNumber(params.id)), getAllDatasets()])
            .then(([metricResponse, datasetsResponse]): void => {
                // Determine if any of the calls failed
                if (
                    metricResponse.status === PROMISES.REJECTED ||
                    datasetsResponse.status === PROMISES.REJECTED
                ) {
                    const axiosError =
                        datasetsResponse.status === PROMISES.REJECTED
                            ? datasetsResponse.reason
                            : metricResponse.status === PROMISES.REJECTED
                            ? metricResponse.reason
                            : ({} as AxiosError);
                    const errMessages = getErrorMessages(axiosError);
                    isEmpty(errMessages)
                        ? notify(
                              NotificationTypeV1.Error,
                              t("message.error-while-fetching", {
                                  entity: t(
                                      datasetsResponse.status ===
                                          PROMISES.REJECTED
                                          ? "label.datasets"
                                          : "label.metric"
                                  ),
                              })
                          )
                        : errMessages.map((err) =>
                              notify(NotificationTypeV1.Error, err)
                          );
                }

                // Attempt to gather data
                if (metricResponse.status === PROMISES.FULFILLED) {
                    setMetric(metricResponse.value);
                }
                if (datasetsResponse.status === PROMISES.FULFILLED) {
                    setDatasets(datasetsResponse.value);
                }
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
                title={t("label.update-entity", {
                    entity: t("label.metric"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {metric && (
                        <MetricsWizard
                            datasets={datasets}
                            metric={metric}
                            onFinish={onMetricWizardFinish}
                        />
                    )}
                    {/* No data available message */}
                    {!metric && <NoDataIndicator />}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
