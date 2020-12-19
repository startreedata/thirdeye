import { Grid } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AlertEvaluationTimeSeriesCard } from "../../components/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { getAlertEvaluation } from "../../rest/alert-rest/alert-rest";
import { getAnomaly } from "../../rest/anomaly-rest/anomaly-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import {
    getAnomalyCardData,
    getAnomalyName,
} from "../../utils/anomaly-util/anomaly-util";
import { isValidNumberId } from "../../utils/params-util/params-util";
import { getAnomaliesDetailPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";
import { AnomaliesDetailPageParams } from "./anomalies-detail-page.interfaces";

export const AnomaliesDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [anomaly, setAnomaly] = useState<Anomaly>({} as Anomaly);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const params = useParams<AnomaliesDetailPageParams>();
    const { enqueueSnackbar } = useSnackbar();

    const [
        appTimeRange,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRange,
        state.getAppTimeRangeDuration,
    ]);
    const [chartData, setChartData] = useState<AlertEvaluation | null>();
    const { t } = useTranslation();

    useEffect(() => {
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.anomaly"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            return;
        }

        let anomaly = {} as Anomaly;
        try {
            anomaly = await getAnomaly(toNumber(params.id));
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } finally {
            setAnomaly(anomaly);

            // Create page breadcrumbs
            setPageBreadcrumbs([
                {
                    text: getAnomalyName(anomaly),
                    path: getAnomaliesDetailPath(anomaly.id),
                },
            ]);
        }
    };

    // To fetch chartData on dateRange Change
    useEffect(() => {
        const init = async (): Promise<void> => {
            setChartData(null);

            setChartData(await fetchChartData());
        };

        init();
    }, [appTimeRange, anomaly?.alert?.id]);

    const fetchChartData = async (): Promise<AlertEvaluation | null> => {
        const { startTime, endTime } = getAppTimeRangeDuration();

        if (
            !isValidNumberId(anomaly?.alert?.id + "") ||
            !startTime ||
            !endTime
        ) {
            // To turn off the loader
            return {} as AlertEvaluation;
        }
        let chartData = null;
        try {
            const alertEvalution = {
                alert: ({ id: anomaly?.alert?.id } as unknown) as Alert,
                start: startTime,
                end: endTime,
            } as AlertEvaluation;

            chartData = await getAlertEvaluation(alertEvalution);
        } catch (e) {
            chartData = {} as AlertEvaluation;
        }

        return chartData;
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
            <PageContents centerAlign title={getAnomalyName(anomaly)}>
                {!isEmpty(anomaly) && (
                    <Grid container>
                        <Grid item md={12}>
                            <AnomalyCard
                                hideViewDetailsLinks
                                anomaly={getAnomalyCardData(anomaly)}
                            />
                        </Grid>
                        <Grid item md={12}>
                            <AlertEvaluationTimeSeriesCard
                                alertEvaluation={chartData as AlertEvaluation}
                            />
                        </Grid>
                    </Grid>
                )}
            </PageContents>
        </PageContainer>
    );
};
