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
import { AxiosError } from "axios";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import {
    createDatasource,
    onboardAllDatasets,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { createDefaultDatasource } from "../../utils/datasources/datasources.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getDatasourcesAllPath,
    getDatasourcesViewPath,
} from "../../utils/routes/routes.util";

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
                            notifyIfErrors(
                                ActionStatus.Error,
                                getErrorMessages(error),
                                notify,
                                t("message.onboard-error", {
                                    entity: t("label.datasets"),
                                })
                            );
                        });

                // Redirect to datasources detail path
                navigate(getDatasourcesViewPath(datasource.id));
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.create-error", {
                        entity: t("label.datasource"),
                    })
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

            <DatasourceWizard
                isCreate
                datasource={createDefaultDatasource()}
                submitBtnLabel={t("label.create-entity", {
                    entity: t("label.datasource"),
                })}
                onCancel={() => navigate(getDatasourcesAllPath())}
                onSubmit={onDatasourceWizardFinish}
            />
        </PageV1>
    );
};
