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
import { Box, Button, Grid, Link } from "@material-ui/core";
import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { DatasourceListV1 } from "../../components/datasource-list-v1/datasource-list-v1.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDatasources } from "../../rest/datasources/datasources.actions";
import { deleteDatasource } from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { getUiDatasources } from "../../utils/datasources/datasources.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getDatasourcesCreatePath } from "../../utils/routes/routes.util";

export const DatasourcesAllPage: FunctionComponent = () => {
    const { getDatasources, status, errorMessages } = useGetDatasources();
    const [uiDatasources, setUiDatasources] = useState<UiDatasource[]>([]);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch datasources
        fetchAllDatasources();
    }, []);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasources"),
            })
        );
    }, [status]);

    const fetchAllDatasources = (): void => {
        setUiDatasources([]);

        getDatasources().then((datasources) => {
            if (datasources) {
                setUiDatasources(getUiDatasources(datasources));
            }
        });
    };

    const handleDatasourceDelete = (uiDatasource: UiDatasource): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiDatasource.name,
            }),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleDatasourceDeleteOk(uiDatasource),
        });
    };

    const handleDatasourceDeleteOk = (uiDatasource: UiDatasource): void => {
        deleteDatasource(uiDatasource.id)
            .then((datasource) => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.datasource"),
                    })
                );

                // Remove deleted datasource from fetched datasources
                removeUiDatasource(datasource);
            })
            .catch((error: AxiosError) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.delete-error", {
                        entity: t("label.datasource"),
                    })
                );
            });
    };

    const removeUiDatasource = (datasource: Datasource): void => {
        if (!datasource) {
            return;
        }

        setUiDatasources(
            (uiDatasources) =>
                uiDatasources &&
                uiDatasources.filter(
                    (uiDatasource) => uiDatasource.id !== datasource.id
                )
        );
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={0} />
            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    isError={status === ActionStatus.Error}
                    isLoading={status === ActionStatus.Working}
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
                                                            "label.datasources"
                                                        ),
                                                    }
                                                )}{" "}
                                                <Link
                                                    href="https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/how-tos/database/"
                                                    target="_blank"
                                                >
                                                    {t(
                                                        "message.view-documentation"
                                                    )}
                                                </Link>{" "}
                                                {t(
                                                    "message.on-how-to-create-entity",
                                                    {
                                                        entity: t(
                                                            "label.datasource"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                or
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                <Button
                                                    color="primary"
                                                    href={getDatasourcesCreatePath()}
                                                >
                                                    {t("label.create-entity", {
                                                        entity: t(
                                                            "label.datasource"
                                                        ),
                                                    })}
                                                </Button>
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={uiDatasources.length === 0}
                    >
                        <DatasourceListV1
                            datasources={uiDatasources}
                            onDelete={handleDatasourceDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
