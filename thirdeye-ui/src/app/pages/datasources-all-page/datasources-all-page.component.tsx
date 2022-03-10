import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { DatasourceListV1 } from "../../components/datasource-list-v1/datasource-list-v1.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import {
    deleteDatasource,
    getAllDatasources,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { getUiDatasources } from "../../utils/datasources/datasources.util";

export const DatasourcesAllPage: FunctionComponent = () => {
    const [uiDatasources, setUiDatasources] = useState<UiDatasource[] | null>(
        null
    );
    const { showDialog } = useDialog();
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
            .catch(() => {
                notify(NotificationTypeV1.Error, t("message.fetch-error"));
            })
            .finally(() => {
                setUiDatasources(fetchedUiDatasources);
            });
    };

    const handleDatasourceDelete = (uiDatasource: UiDatasource): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiDatasource.name }),
            okButtonLabel: t("label.delete"),
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
            .catch(() =>
                notify(
                    NotificationTypeV1.Error,
                    t("message.delete-error", {
                        entity: t("label.datasource"),
                    })
                )
            );
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
