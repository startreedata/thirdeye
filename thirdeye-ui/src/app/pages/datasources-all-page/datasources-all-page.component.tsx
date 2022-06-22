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
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { DatasourceListV1 } from "../../components/datasource-list-v1/datasource-list-v1.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import {
    deleteDatasource,
    getAllDatasources,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { getUiDatasources } from "../../utils/datasources/datasources.util";
import { getErrorMessages } from "../../utils/rest/rest.util";

export const DatasourcesAllPage: FunctionComponent = () => {
    const [uiDatasources, setUiDatasources] = useState<UiDatasource[] | null>(
        null
    );
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch datasources
        fetchAllDatasources();
    }, []);

    const fetchAllDatasources = (): void => {
        setUiDatasources(null);

        let fetchedUiDatasources: UiDatasource[] = [];
        getAllDatasources()
            .then((datasources) => {
                fetchedUiDatasources = getUiDatasources(datasources);
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.error-while-fetching", {
                              entity: t("label.datasources"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            })
            .finally(() => {
                setUiDatasources(fetchedUiDatasources);
            });
    };

    const handleDatasourceDelete = (uiDatasource: UiDatasource): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiDatasource.name,
            }),
            okButtonText: t("label.delete"),
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
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.delete-error", {
                              entity: t("label.datasource"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
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
                <DatasourceListV1
                    datasources={uiDatasources}
                    onDelete={handleDatasourceDelete}
                />
            </PageContentsGridV1>
        </PageV1>
    );
};
