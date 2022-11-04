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
