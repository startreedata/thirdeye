/*
 * Copyright 2022 StarTree Inc
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
import { Grid } from "@material-ui/core";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CohortsTable } from "../../components/cohort-detector/cohorts-table/cohorts-table.component";
import { DatasetDetails } from "../../components/cohort-detector/dataset-details/dataset-details.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { useGetCohort } from "../../rest/rca/rca.actions";
import { GetCohortParams } from "../../rest/rca/rca.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";

export const CohortDetectorPage: FunctionComponent = () => {
    const { cohortsResponse, getCohorts, status, errorMessages } =
        useGetCohort();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.cohorts"),
            })
        );
    }, [status]);

    const handleSearchButtonClick = (
        getCohortsParams: GetCohortParams
    ): void => {
        getCohorts(getCohortsParams);
    };

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                title="Automated cohort recommender"
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <DatasetDetails
                        onSearchButtonClick={handleSearchButtonClick}
                    />
                </Grid>
                <Grid item xs={12}>
                    <CohortsTable
                        cohortsData={cohortsResponse}
                        getCohortsRequestStatus={status}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
