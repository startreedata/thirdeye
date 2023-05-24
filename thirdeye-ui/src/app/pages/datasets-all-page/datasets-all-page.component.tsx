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
import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { DatasetListV1 } from "../../components/dataset-list-v1/dataset-list-v1.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { deleteDataset } from "../../rest/datasets/datasets.rest";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { deleteMetric } from "../../rest/metrics/metrics.rest";
import {
    makeDeleteRequest,
    promptDeleteConfirmation,
} from "../../utils/bulk-delete/bulk-delete.util";
import { getUiDatasets } from "../../utils/datasets/datasets.util";
import { useGetDatasourcesTree } from "../../utils/datasources/use-get-datasources-tree.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getDatasetsOnboardPath } from "../../utils/routes/routes.util";

export const DatasetsAllPage: FunctionComponent = () => {
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [uiDatasets, setUiDatasets] = useState<UiDataset[]>([]);

    const { datasetsInfo, getDatasetsHook } = useGetDatasourcesTree();

    useEffect(() => {
        notifyIfErrors(
            getDatasetsHook.status,
            getDatasetsHook.errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasets"),
            })
        );
    }, [getDatasetsHook.status]);

    useEffect(() => {
        if (!datasetsInfo) {
            return;
        }

        const flattenedDatasets = datasetsInfo.map(
            (datasetIndo) => datasetIndo.dataset
        );
        setUiDatasets(getUiDatasets(flattenedDatasets));
    }, [datasetsInfo]);

    const handleDatasetDelete = (uiDatasetsToDelete: UiDataset[]): void => {
        promptDeleteConfirmation(
            uiDatasetsToDelete,
            () => {
                uiDatasets &&
                    makeDeleteRequest(
                        uiDatasetsToDelete,
                        deleteDataset,
                        t,
                        notify,
                        t("label.dataset"),
                        t("label.datasets")
                    ).then((deleted) => {
                        deleted.forEach((dataset) => {
                            if (datasetsInfo) {
                                const datasetInfo = datasetsInfo.find(
                                    (c) => c.dataset.id === dataset.id
                                );

                                if (datasetInfo) {
                                    datasetInfo.metrics.forEach((metric) => {
                                        metric.id > 0 &&
                                            deleteMetric(metric.id);
                                    });
                                }
                            }
                        });
                        setUiDatasets(() => {
                            return [...uiDatasets].filter((candidate) => {
                                return (
                                    deleted.findIndex(
                                        (d) => d.id === candidate.id
                                    ) === -1
                                );
                            });
                        });
                    });
            },
            t,
            showDialog,
            t("label.datasets")
        );
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={1} />
            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    wrapInCard
                    wrapInGrid
                    isError={getDatasetsHook.status === ActionStatus.Error}
                    isLoading={
                        getDatasetsHook.status === ActionStatus.Working ||
                        getDatasetsHook.status === ActionStatus.Initial
                    }
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <Box padding={20}>
                                        <NoDataIndicator>
                                            <Box textAlign="center">
                                                {t(
                                                    "message.no-entity-created",
                                                    {
                                                        entity: t(
                                                            "label.datasets"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                <Button
                                                    color="primary"
                                                    href={getDatasetsOnboardPath()}
                                                >
                                                    {t("label.create-entity", {
                                                        entity: t(
                                                            "label.dataset"
                                                        ),
                                                    })}
                                                </Button>
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={uiDatasets.length === 0}
                    >
                        <DatasetListV1
                            datasets={uiDatasets}
                            onDelete={handleDatasetDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
