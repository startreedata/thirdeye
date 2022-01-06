import { Grid } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
    PageContentsGridV1,
    PageV1,
} from "@startree-ui/platform-ui";
import { assign, toNumber } from "lodash";
import { useSnackbar } from "notistack";
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
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { DatasourcesUpdatePageParams } from "./datasources-update-page.interfaces";

export const DatasourcesUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [datasource, setDatasource] = useState<Datasource>();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<DatasourcesUpdatePageParams>();

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
                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.datasource"),
                    }),
                    getSuccessSnackbarOption()
                );
                // Redirect to datasources detail path
                history.push(getDatasourcesViewPath(datasourceResponse.id));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", {
                        entity: t("label.datasource"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchDataSource = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.datasource"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

            return;
        }

        getDatasource(toNumber(params.id))
            .then((datasource) => {
                setDatasource(datasource);
            })
            .catch(() => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
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
