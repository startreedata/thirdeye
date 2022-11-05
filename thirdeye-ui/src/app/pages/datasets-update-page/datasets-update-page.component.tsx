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
import { AxiosError } from "axios";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { DatasetWizard } from "../../components/dataset-wizard/dataset-wizard.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { useGetDataset } from "../../rest/datasets/datasets.actions";
import { updateDataset } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getDatasetsAllPath,
    getDatasetsViewPath,
} from "../../utils/routes/routes.util";
import { DatasetsUpdatePageParams } from "./datasets-update-page.interfaces";

export const DatasetsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { dataset, getDataset, status, errorMessages } = useGetDataset();
    const params = useParams<DatasetsUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchDataset();
    }, []);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.dataset"),
            })
        );
    }, [status]);

    const handleCancelClick = (): void => {
        navigate(getDatasetsAllPath());
    };

    const onDatasetWizardFinish = (dataset: Dataset): void => {
        if (!dataset) {
            return;
        }

        updateDataset(dataset)
            .then((dataset: Dataset): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
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
                          t("message.update-error", {
                              entity: t("label.dataset"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    const fetchDataset = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.dataset"),
                    id: params.id,
                })
            );

            return;
        }

        getDataset(toNumber(params.id)).finally((): void => {
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
                    entity: t("label.dataset"),
                })}
            />
            {dataset && (
                <DatasetWizard
                    dataset={dataset}
                    submitBtnLabel={t("label.update-entity", {
                        entity: t("label.dataset"),
                    })}
                    onCancel={handleCancelClick}
                    onSubmit={onDatasetWizardFinish}
                />
            )}

            {/* No data available message */}
            {!dataset && <NoDataIndicator />}
        </PageV1>
    );
};
