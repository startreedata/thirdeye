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
import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { DatasourceCard } from "../../components/entity-cards/datasource-card/datasource-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
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
import {
    deleteDatasource,
    getDatasource,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { getUiDatasource } from "../../utils/datasources/datasources.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getDatasourcesAllPath } from "../../utils/routes/routes.util";
import { DatasourcesViewPageParams } from "./datasources-view-page.interfaces";

export const DatasourcesViewPage: FunctionComponent = () => {
    const [uiDatasource, setUiDatasource] = useState<UiDatasource | null>(null);
    const { showDialog } = useDialogProviderV1();
    const params = useParams<DatasourcesViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch datasource
        fetchDatasource();
    }, []);

    const fetchDatasource = (): void => {
        setUiDatasource(null);
        let fetchedUiDatasource = {} as UiDatasource;

        if (params.id && !isValidNumberId(params.id)) {
            // Invalid id
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.datasource"),
                    id: params.id,
                })
            );

            setUiDatasource(fetchedUiDatasource);

            return;
        }

        getDatasource(toNumber(params.id))
            .then((datasource) => {
                fetchedUiDatasource = getUiDatasource(datasource);
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
            .finally(() => {
                setUiDatasource(fetchedUiDatasource);
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
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.datasource"),
                    })
                );

                // Redirect to datasources all path
                navigate(getDatasourcesAllPath());
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

    return (
        <PageV1>
            <PageHeader
                showCreateButton
                title={uiDatasource ? uiDatasource.name : ""}
            />
            <PageContentsGridV1>
                {/* Datasource */}
                <Grid item xs={12}>
                    <DatasourceCard
                        uiDatasource={uiDatasource}
                        onDelete={handleDatasourceDelete}
                    />
                </Grid>

                {/* Datasource JSON viewer */}
                <Grid item sm={12}>
                    <JSONEditorV1<Datasource>
                        hideValidationSuccessIcon
                        readOnly
                        value={uiDatasource?.datasource as Datasource}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
