import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { assign, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { DatasourceWizard } from "../../components/datasource-wizard/datasource-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import {
    getDatasource,
    updateDatasource,
} from "../../rest/datasources/datasources.rest";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { omitNonUpdatableData } from "../../utils/datasources/datasources.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessage } from "../../utils/rest/rest.util";
import { getDatasourcesViewPath } from "../../utils/routes/routes.util";
import { DatasourcesUpdatePageParams } from "./datasources-update-page.interfaces";

export const DatasourcesUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasource, setDatasource] = useState<Datasource>();
    const params = useParams<DatasourcesUpdatePageParams>();
    const { notify } = useNotificationProviderV1();

    const navigate = useNavigate();
    const { t } = useTranslation();

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
                navigate(getDatasourcesViewPath(datasourceResponse.id));
            })
            .catch((error: AxiosError): void => {
                const errMessage = getErrorMessage(error);

                notify(
                    NotificationTypeV1.Error,
                    errMessage ||
                        t("message.update-error", {
                            entity: t("label.datasource"),
                        })
                );
            });
    };

    const fetchDataSource = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
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
            .catch((error: AxiosError) => {
                const errMessage = getErrorMessage(error);

                notify(
                    NotificationTypeV1.Error,
                    errMessage || t("message.fetch-error")
                );
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
