import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
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
        <PageContents centered title={t("label.create")}>
            <DatasourceWizard onFinish={onDatasourceWizardFinish} />
        </PageContents>
    );
};
