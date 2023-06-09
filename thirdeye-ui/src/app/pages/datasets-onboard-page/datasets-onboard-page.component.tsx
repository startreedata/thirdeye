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
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { DatasetCreateWizard } from "../../components/dataset-create-wizard/dataset-create-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { createDatasets } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getDatasetsAllPath,
    getDatasetsViewPath,
} from "../../utils/routes/routes.util";

export const DatasetsOnboardPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const handleSubmit = (datasets: Dataset[]): void => {
        if (isEmpty(datasets)) {
            return;
        }

        createDatasets(datasets)
            .then((datasets: Dataset[]): void => {
                if (datasets.length === 1) {
                    // Redirect to datasets detail path
                    navigate(getDatasetsViewPath(datasets[0].id));
                } else {
                    navigate(getDatasetsAllPath());
                }

                notify(
                    NotificationTypeV1.Success,
                    t("message.onboard-success", {
                        entity:
                            datasets.length > 1
                                ? t("label.datasets")
                                : t("label.dataset"),
                    })
                );
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.onboard-error", {
                        entity: t("label.dataset"),
                    })
                );
            });
    };

    return (
        <PageV1>
            <PageHeader
                title={t("label.onboard-entity", {
                    entity: t("label.dataset"),
                })}
            />
            <DatasetCreateWizard onSubmit={handleSubmit} />
        </PageV1>
    );
};
