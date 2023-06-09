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
import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { DatasetCard } from "../../components/entity-cards/dataset-card/dataset-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    JSONEditorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDataset } from "../../rest/datasets/datasets.actions";
import { deleteDataset } from "../../rest/datasets/datasets.rest";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { deleteMetric } from "../../rest/metrics/metrics.rest";
import { getUiDataset } from "../../utils/datasets/datasets.util";
import { useGetDatasourcesTree } from "../../utils/datasources/use-get-datasources-tree.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getDatasetsAllPath,
    getDatasetsUpdatePath,
} from "../../utils/routes/routes.util";
import { DatasetsViewPageParams } from "./dataset-view-page.interfaces";

export const DatasetsViewPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { id: datasetId } = useParams<DatasetsViewPageParams>();
    const { notify } = useNotificationProviderV1();
    const { showDialog } = useDialogProviderV1();

    const { dataset, getDataset, status, errorMessages } = useGetDataset();

    const { datasetsInfo } = useGetDatasourcesTree();

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

    useEffect(() => {
        getDataset(toNumber(datasetId));
    }, [datasetId]);

    const handleDatasetDelete = (uiDataset: UiDataset): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiDataset.name,
            }),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleDatasetDeleteOk(uiDataset),
        });
    };

    const handleDatasetDeleteOk = (uiDataset: UiDataset): void => {
        deleteDataset(uiDataset.id)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", { entity: t("label.dataset") })
                );

                if (datasetsInfo) {
                    const datasetInfo = datasetsInfo.find(
                        (c) => c.dataset.id === uiDataset.id
                    );

                    if (datasetInfo) {
                        datasetInfo.metrics.forEach((metric) => {
                            metric.id > 0 && deleteMetric(metric.id);
                        });
                    }
                }

                // Redirect to datasets all path
                navigate(getDatasetsAllPath());
            })
            .catch((error: AxiosError) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.delete-error", {
                        entity: t("label.dataset"),
                    })
                );
            });
    };

    const handleDatasetEdit = (id: number): void => {
        navigate(getDatasetsUpdatePath(id));
    };

    return (
        <PageV1>
            <PageHeader showCreateButton title={dataset ? dataset.name : ""} />
            <PageContentsGridV1>
                <LoadingErrorStateSwitch
                    isError={status === ActionStatus.Error}
                    isLoading={
                        status === ActionStatus.Initial ||
                        status === ActionStatus.Working
                    }
                >
                    <Grid item xs={12}>
                        {dataset && (
                            <DatasetCard
                                uiDataset={getUiDataset(dataset)}
                                onDelete={handleDatasetDelete}
                                onEdit={handleDatasetEdit}
                            />
                        )}
                    </Grid>
                    <Grid item xs={12}>
                        <JSONEditorV1 value={dataset} />
                    </Grid>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
