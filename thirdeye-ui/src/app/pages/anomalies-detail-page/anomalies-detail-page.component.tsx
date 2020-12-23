import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AlertEvaluationTimeSeriesCard } from "../../components/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { AnomalyCardData } from "../../components/anomaly-card/anomaly-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { getAlertEvaluation } from "../../rest/alert-rest/alert-rest";
import { getAnomaly } from "../../rest/anomaly-rest/anomaly-rest";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import {
    createAlertEvaluation,
    createEmptyAnomalyCardData,
    getAnomalyCardData,
} from "../../utils/anomaly-util/anomaly-util";
import { isValidNumberId } from "../../utils/params-util/params-util";
import { getAnomaliesDetailPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";
import { AnomaliesDetailPageParams } from "./anomalies-detail-page.interfaces";

export const AnomaliesDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [anomalyCardData, setAnomalyCardData] = useState<AnomalyCardData>(
        createEmptyAnomalyCardData()
    );
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [
        appTimeRangeDuration,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRangeDuration,
        state.getAppTimeRangeDuration,
    ]);
    const params = useParams<AnomaliesDetailPageParams>();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: anomalyCardData
                    ? anomalyCardData.name
                    : t("label.no-data-available-marker"),
                pathFn: (): string => {
                    return anomalyCardData
                        ? getAnomaliesDetailPath(anomalyCardData.id)
                        : "";
                },
            },
        ]);
    }, [anomalyCardData]);

    useEffect(() => {
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, [params.id]);

    useEffect(() => {
        // Fetch visualization data
        const init = async (): Promise<void> => {
            setAlertEvaluation(null);

            await fetchVisualizationData();
        };

        init();
    }, [anomalyCardData.alertId, appTimeRangeDuration]);

    const fetchData = async (): Promise<void> => {
        let fetchedAnomalyCardData = createEmptyAnomalyCardData();

        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.anomaly"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            setAnomalyCardData(fetchedAnomalyCardData);

            return;
        }

        try {
            fetchedAnomalyCardData = getAnomalyCardData(
                await getAnomaly(toNumber(params.id))
            );
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } finally {
            setAnomalyCardData(fetchedAnomalyCardData);
        }
    };

    const fetchVisualizationData = async (): Promise<void> => {
        let fetchedAlertEvaluation = {} as AlertEvaluation;

        if (!anomalyCardData || anomalyCardData.alertId < 0) {
            setAlertEvaluation(fetchedAlertEvaluation);

            return;
        }

        const timeRangeDuration = getAppTimeRangeDuration();
        try {
            fetchedAlertEvaluation = await getAlertEvaluation(
                createAlertEvaluation(
                    anomalyCardData.alertId,
                    timeRangeDuration.startTime,
                    timeRangeDuration.endTime
                )
            );
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        }

        setAlertEvaluation(fetchedAlertEvaluation);
    };

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents
                contentsCenterAlign
                title={
                    anomalyCardData
                        ? anomalyCardData.name
                        : t("label.no-data-available-marker")
                }
            >
                <Grid container md={12}>
                    {/* Anomaly */}
                    <Grid item md={12}>
                        <AnomalyCard
                            hideViewDetailsLinks
                            anomaly={anomalyCardData}
                        />
                    </Grid>

                    {/* Alert evaluation time series */}
                    <Grid item md={12}>
                        <AlertEvaluationTimeSeriesCard
                            alertEvaluation={alertEvaluation}
                        />
                    </Grid>
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
