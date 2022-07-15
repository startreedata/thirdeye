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
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { onBoardDataset } from "../../rest/datasets/datasets.rest";
import { getAllDatasources } from "../../rest/datasources/datasources.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";

export const DatasetsOnboardPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasources, setDatasources] = useState<Datasource[]>([]);
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchAllDatasources();
    }, []);

    const onDatasetWizardFinish = (dataset: Dataset): void => {
        if (!dataset) {
            return;
        }

        onBoardDataset(dataset.name, dataset.dataSource.name)
            .then((dataset: Dataset): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.onboard-success", {
                        entity: t("label.dataset"),
                    })
                );

                // Redirect to datasets detail path
                navigate(getDatasetsViewPath(dataset.id));
            })
            .catch((error: AxiosError): void => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.onboard-error", {
                              entity: t("label.dataset"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    const fetchAllDatasources = (): void => {
        getAllDatasources()
            .then((datasources: Datasource[]): void => {
                setDatasources(datasources);
            })
            .catch((error: AxiosError): void => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.error-while-fetching", {
                              entity: t("label.datasources"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
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
        <PageV1>
            <PageHeader
                title={t("label.onboard-entity", {
                    entity: t("label.dataset"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <DatasetWizard
                        datasources={datasources}
                        onFinish={onDatasetWizardFinish}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
