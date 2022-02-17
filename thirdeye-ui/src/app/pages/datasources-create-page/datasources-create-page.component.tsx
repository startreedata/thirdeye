import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { createDatasource } from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { getDatasourcesViewPath } from "../../utils/routes/routes.util";

export const DatasourcesCreatePage: FunctionComponent = () => {
    const history = useHistory();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const onDatasourceWizardFinish = (datasource: Datasource): void => {
        if (!datasource) {
            return;
        }

        createDatasource(datasource)
            .then((datasource: Datasource): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.datasource"),
                    })
                );
                // Redirect to datasources detail path
                history.push(getDatasourcesViewPath(datasource.id));
            })
            .catch((): void => {
                notify(
                    NotificationTypeV1.Error,
                    t("message.create-error", {
                        entity: t("label.datasource"),
                    })
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
