/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid, Typography } from "@material-ui/core";
import { AxiosError } from "axios";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { JSONEditorWithLocalCache } from "../../components/json-editor-with-local-cache/json-editor-with-local-cache.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { WizardBottomBar } from "../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetDataset } from "../../rest/datasets/datasets.actions";
import { updateDataset } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getDatasetsViewPath } from "../../utils/routes/routes.util";
import { DatasetsUpdatePageParams } from "./datasets-update-page.interfaces";

export const DatasetsUpdatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const params = useParams<DatasetsUpdatePageParams>();

    const { dataset, getDataset, status, errorMessages } = useGetDataset();

    const [loading, setLoading] = useState(true);
    const [modifiedDataset, setModifiedDataset] = useState<Dataset>();

    useEffect(() => {
        fetchDataset();
    }, []);

    useEffect(() => {
        if (dataset) {
            setModifiedDataset(dataset);
        }
    }, [dataset]);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.dataset"),
            })
        );
    }, [status]);

    const onDatasetWizardFinish = (dataset: Dataset): void => {
        if (!dataset) {
            return;
        }

        updateDataset(dataset)
            .then((dataset: Dataset): void => {
                // Redirect to datasets detail path
                navigate(getDatasetsViewPath(dataset.id));

                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.dataset"),
                    })
                );
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.update-error", {
                        entity: t("label.dataset"),
                    })
                );
            });
    };

    const fetchDataset = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.dataset"),
                    id: params.id,
                })
            );

            return;
        }

        getDataset(toNumber(params.id)).finally((): void => {
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
                    entity: t("label.dataset"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Grid container>
                            <Grid item xs={12}>
                                <Typography variant="h5">
                                    {t("label.dataset-configuration")}
                                </Typography>
                            </Grid>
                            <Grid item xs={12}>
                                {dataset && (
                                    <JSONEditorWithLocalCache
                                        initialValue={
                                            modifiedDataset as unknown as Record<
                                                string,
                                                unknown
                                            >
                                        }
                                        onChange={(value: string) =>
                                            setModifiedDataset(
                                                JSON.parse(value)
                                            )
                                        }
                                    />
                                )}
                            </Grid>
                        </Grid>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <WizardBottomBar
                backBtnLink={getDatasetsViewPath(Number(params.id))}
                handleNextClick={() =>
                    !!modifiedDataset && onDatasetWizardFinish(modifiedDataset)
                }
                nextButtonLabel={t("label.submit")}
            />

            {/* No data available message */}
            {!dataset && <NoDataIndicator />}
        </PageV1>
    );
};
