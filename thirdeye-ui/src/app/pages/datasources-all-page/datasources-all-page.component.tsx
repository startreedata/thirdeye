import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasourceList } from "../../components/datasource-list/datasource-list.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteDatasource,
    getAllDatasources,
    updateDatasource,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import {
    getUiDatasource,
    getUiDatasources,
} from "../../utils/datasources/datasources.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const DatasourcesAllPage: FunctionComponent = () => {
    const [uiDatasources, setUiDatasources] = useState<UiDatasource[] | null>(
        null
    );
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch datasources
        fetchAllDatasources();
    }, [timeRangeDuration]);

    const fetchAllDatasources = (): void => {
        setUiDatasources(null);

        let fetchedUiDatasources: UiDatasource[] = [];
        getAllDatasources()
            .then((datasources) => {
                fetchedUiDatasources = getUiDatasources(datasources);
            })
            .catch(() => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally(() => {
                setUiDatasources(fetchedUiDatasources);
            });
    };

    const handleDatasourceChange = (uiDatasource: UiDatasource): void => {
        if (!uiDatasource.datasource) {
            return;
        }

        updateDatasource(uiDatasource.datasource)
            .then((datasource) => {
                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.datasource"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Replace updated datasource in fetched datasources
                replaceUiDatasource(datasource);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.update-error", {
                        entity: t("label.datasource"),
                    }),
                    getErrorSnackbarOption()
                )
            );
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
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.datasource"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted datasource from fetched datasources
                removeUiDatasource(datasource);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.datasource"),
                    }),
                    getErrorSnackbarOption()
                )
            );
    };

    const replaceUiDatasource = (datasource: Datasource): void => {
        if (!datasource) {
            return;
        }

        setUiDatasources(
            (uiDatasources) =>
                uiDatasources &&
                uiDatasources.map((uiDatasource) => {
                    if (uiDatasource.id === datasource.id) {
                        // Replace
                        return getUiDatasource(datasource);
                    }

                    return uiDatasource;
                })
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
        <PageContents
            centered
            hideAppBreadcrumbs
            title={t("label.datasources")}
        >
            {/* Datasource list */}
            <DatasourceList
                datasources={uiDatasources}
                onChange={handleDatasourceChange}
                onDelete={handleDatasourceDelete}
            />
        </PageContents>
    );
};
