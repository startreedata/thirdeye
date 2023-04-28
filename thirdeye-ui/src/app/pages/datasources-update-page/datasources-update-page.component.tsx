/*
 * Copyright 2023 StarTree Inc
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
import { assign, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import {
    getDatasource,
    updateDatasource,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getDatasourcesAllPath,
    getDatasourcesViewPath,
} from "../../utils/routes/routes.util";
import { DatasourcesUpdatePageParams } from "./datasources-update-page.interfaces";

export const DatasourcesUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasource, setDatasource] = useState<Datasource>();
    const params = useParams<DatasourcesUpdatePageParams>();
    const { notify } = useNotificationProviderV1();

    const navigate = useNavigate();
    const { t } = useTranslation();

    useEffect(() => {
        const init = async (): Promise<void> => {
            setLoading(false);
        };

        init();
        fetchDataSource();
    }, []);

    const onDatasourceWizardFinish = (newDatasource: Datasource): void => {
        if (!newDatasource) {
            return;
        }

        newDatasource = assign({ ...newDatasource }, { id: datasource?.id });

        updateDatasource(newDatasource)
            .then((datasourceResponse: Datasource): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.datasource"),
                    })
                );
                // Redirect to datasources detail path
                navigate(getDatasourcesViewPath(datasourceResponse.id));
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.update-error", {
                        entity: t("label.datasource"),
                    })
                );
            });
    };

    const fetchDataSource = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.datasource"),
                    id: params.id,
                })
            );
            setLoading(false);

            return;
        }

        getDatasource(toNumber(params.id))
            .then((datasource) => {
                setDatasource(datasource);
            })
            .catch((error: AxiosError) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.error-while-fetching", {
                        entity: t("label.datasource"),
                    })
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
                title={t("label.update-entity", {
                    entity: t("label.datasource"),
                })}
            />
            {datasource && (
                <DatasourceWizard
                    datasource={datasource}
                    submitBtnLabel={t("label.update-entity", {
                        entity: t("label.datasource"),
                    })}
                    onCancel={() => navigate(getDatasourcesAllPath())}
                    onSubmit={onDatasourceWizardFinish}
                />
            )}
        </PageV1>
    );
};
