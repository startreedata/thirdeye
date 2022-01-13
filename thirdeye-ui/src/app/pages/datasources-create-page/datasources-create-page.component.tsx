import { Grid } from "@material-ui/core";
import { PageContentsGridV1, PageV1 } from "@startree-ui/platform-ui";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { createDatasource } from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { getDatasourcesViewPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const DatasourcesCreatePage: FunctionComponent = () => {
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    const onDatasourceWizardFinish = (datasource: Datasource): void => {
        if (!datasource) {
            return;
        }

        createDatasource(datasource)
            .then((datasource: Datasource): void => {
                enqueueSnackbar(
                    t("message.create-success", {
                        entity: t("label.datasource"),
                    }),
                    getSuccessSnackbarOption()
                );
                // Redirect to datasources detail path
                history.push(getDatasourcesViewPath(datasource.id));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.create-error", {
                        entity: t("label.datasource"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    return (
        <PageV1>
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.datasource"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <DatasourceWizard onFinish={onDatasourceWizardFinish} />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
