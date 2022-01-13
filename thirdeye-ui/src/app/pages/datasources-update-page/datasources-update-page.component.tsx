import { Grid } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { assign, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    getDatasource,
    updateDatasource,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { omitNonUpdatableData } from "../../utils/datasources/datasources.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getDatasourcesViewPath } from "../../utils/routes/routes.util";
import { DatasourcesUpdatePageParams } from "./datasources-update-page.interfaces";

export const DatasourcesUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasource, setDatasource] = useState<Datasource>();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const params = useParams<DatasourcesUpdatePageParams>();
    const { notify } = useNotificationProviderV1();

    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: datasource ? datasource.name : "",
                onClick: (): void => {
                    if (datasource) {
                        history.push(getDatasourcesViewPath(datasource.id));
                    }
                },
            },
        ]);
    }, [datasource]);

    useEffect(() => {
        const init = async (): Promise<void> => {
            setLoading(false);
        };

        init();
        fetchDataSource();
    }, []);

    const onDatasourceWizardFinish = (newDatasource: Datasource): void => {
        if (!newDatasource) {
            return;
        }

        newDatasource = assign({ ...newDatasource }, { id: datasource?.id });

        updateDatasource(newDatasource)
            .then((datasourceResponse: Datasource): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.datasource"),
                    })
                );
                // Redirect to datasources detail path
                history.push(getDatasourcesViewPath(datasourceResponse.id));
            })
            .catch((): void => {
                notify(
                    NotificationTypeV1.Error,
                    t("message.update-error", {
                        entity: t("label.datasource"),
                    })
                );
            });
    };

    const fetchDataSource = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.datasource"),
                    id: params.id,
                })
            );
            setLoading(false);

            return;
        }

        getDatasource(toNumber(params.id))
            .then((datasource) => {
                setDatasource(datasource);
            })
            .catch(() => {
                notify(NotificationTypeV1.Error, t("message.fetch-error"));
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeader
                title={t("label.update-entity", {
                    entity: t("label.datasource"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {datasource && (
                        <DatasourceWizard
                            datasource={omitNonUpdatableData(datasource)}
                            onFinish={onDatasourceWizardFinish}
                        />
                    )}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
