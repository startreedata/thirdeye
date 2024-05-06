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
import { onBoardDataset } from "../../rest/datasets/datasets.rest";
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

    const handleSubmit = (datasets: Dataset[], datasourceId: number): void => {
        if (isEmpty(datasets)) {
            return;
        }

        const datasetsAndPromises: [Dataset, Promise<Dataset>][] = datasets.map(
            (dataset) => [
                dataset,
                onBoardDataset(dataset.name, datasourceId.toString()),
            ]
        );

        datasetsAndPromises.forEach(([, onboardPromise]) => {
            onboardPromise.catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.onboard-error", {
                        entity: t("label.dataset"),
                    })
                );
            });
        });

        Promise.allSettled(
            datasetsAndPromises.map(
                (datasetsAndPromises) => datasetsAndPromises[1]
            )
        ).then((promiseStatuses) => {
            let failures = 0;

            promiseStatuses.forEach((promiseStatus) => {
                if (promiseStatus.status !== "fulfilled") {
                    failures += 1;
                }
            });

            if (promiseStatuses.length === 1 && failures === 0) {
                // Redirect to dataset detail path
                if (
                    promiseStatuses[0].status === "fulfilled" &&
                    promiseStatuses[0].value
                ) {
                    navigate(getDatasetsViewPath(promiseStatuses[0].value.id));
                } else {
                    navigate(getDatasetsAllPath());
                }
            } else {
                navigate(getDatasetsAllPath());
            }

            notify(
                NotificationTypeV1.Success,
                t("message.onboard-success", {
                    entity:
                        promiseStatuses.length > 1
                            ? t("label.datasets")
                            : t("label.dataset"),
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
