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
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    createDatasource,
    onboardAllDatasets,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getDatasourcesViewPath } from "../../utils/routes/routes.util";

export const DatasourcesCreatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const onDatasourceWizardFinish = (
        datasource: Datasource,
        onboardDatasets: boolean
    ): void => {
        if (!datasource) {
            return;
        }

        createDatasource(datasource)
            .then((datasource: Datasource): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.datasource"),
                    })
                );

                // Onboarding datasets won't be a blocker.
                // So we won't stop navigate for this API call
                onboardDatasets &&
                    onboardAllDatasets(datasource.name)
                        .then((): void => {
                            notify(
                                NotificationTypeV1.Success,
                                t("message.onboard-success", {
                                    entity: t("label.datasets"),
                                })
                            );
                        })
                        .catch((error: AxiosError): void => {
                            const errMessages = getErrorMessages(error);

                            isEmpty(errMessages)
                                ? notify(
                                      NotificationTypeV1.Error,
                                      t("message.onboard-error", {
                                          entity: t("label.datasets"),
                                      })
                                  )
                                : errMessages.map((err) =>
                                      notify(NotificationTypeV1.Error, err)
                                  );
                        });

                // Redirect to datasources detail path
                navigate(getDatasourcesViewPath(datasource.id));
            })
            .catch((error: AxiosError): void => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.create-error", {
                              entity: t("label.datasource"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    return (
        <PageV1>
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.datasource"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <DatasourceWizard
                        isCreate
                        onFinish={onDatasourceWizardFinish}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
