import { Grid } from "@material-ui/core";
import {
    JSONEditorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { DatasourceCard } from "../../components/entity-cards/datasource-card/datasource-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteDatasource,
    getDatasource,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { getUiDatasource } from "../../utils/datasources/datasources.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getDatasourcesAllPath } from "../../utils/routes/routes.util";
import { DatasourcesViewPageParams } from "./datasources-view-page.interfaces";

export const DatasourcesViewPage: FunctionComponent = () => {
    const [uiDatasource, setUiDatasource] = useState<UiDatasource | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const params = useParams<DatasourcesViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch datasource
        fetchDatasource();
    }, [timeRangeDuration]);

    const fetchDatasource = (): void => {
        setUiDatasource(null);
        let fetchedUiDatasource = {} as UiDatasource;

        if (!isValidNumberId(params.id)) {
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
            .catch(() => {
                notify(NotificationTypeV1.Error, t("message.fetch-error"));
            })
            .finally(() => {
                setUiDatasource(fetchedUiDatasource);
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
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.datasource"),
                    })
                );

                // Redirect to datasources all path
                history.push(getDatasourcesAllPath());
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

    return (
        <PageV1>
            <PageHeader title={uiDatasource ? uiDatasource.name : ""} />
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
                        readOnly
                        value={uiDatasource?.datasource as Datasource}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
