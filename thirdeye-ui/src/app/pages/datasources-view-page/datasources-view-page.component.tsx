import { Grid } from "@material-ui/core";
import { JSONEditorV1 } from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { DatasourceCard } from "../../components/entity-cards/datasource-card/datasource-card.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteDatasource,
    getDatasource,
} from "../../rest/datasources/datasources.rest";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { getUiDatasource } from "../../utils/datasources/datasources.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getDatasourcesAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { DatasourcesViewPageParams } from "./datasources-view-page.interfaces";

export const DatasourcesViewPage: FunctionComponent = () => {
    const [uiDatasource, setUiDatasource] = useState<UiDatasource | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<DatasourcesViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

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
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.datasource"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            setUiDatasource(fetchedUiDatasource);

            return;
        }

        getDatasource(toNumber(params.id))
            .then((datasource) => {
                fetchedUiDatasource = getUiDatasource(datasource);
            })
            .catch(() => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
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
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.datasource"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to datasources all path
                history.push(getDatasourcesAllPath());
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

    return (
        <PageContents centered title={uiDatasource ? uiDatasource.name : ""}>
            <Grid container>
                {/* Datasource */}
                <Grid item xs={12}>
                    <DatasourceCard
                        uiDatasource={uiDatasource}
                        onDelete={handleDatasourceDelete}
                    />
                </Grid>

                {/* Datasource JSON viewer */}
                <Grid item sm={12}>
                    <JSONEditorV1
                        readOnly
                        value={
                            (uiDatasource?.datasource as unknown) as Record<
                                string,
                                unknown
                            >
                        }
                    />
                </Grid>
            </Grid>
        </PageContents>
    );
};
