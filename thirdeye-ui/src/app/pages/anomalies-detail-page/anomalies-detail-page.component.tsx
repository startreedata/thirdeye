import { Grid } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { TimeSeriesChartCard } from "../../components/timeseries-chart-card/timeseries-chart-card.component";
import { getAlertEvaluation } from "../../rest/alert-rest/alert-rest";
import { getAnomaly } from "../../rest/anomaly-rest/anomaly-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs-store/application-breadcrumbs-store";
import { useDateRangePickerStore } from "../../store/date-range-picker/date-range-picker-store";
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
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const params = useParams<AnomaliesDetailPageParams>();
    const { enqueueSnackbar } = useSnackbar();

    const [dateRange] = useDateRangePickerStore((state) => [state.dateRange]);
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
    }, [dateRange, anomaly?.alert?.id]);

    const fetchChartData = async (): Promise<AlertEvaluation | null> => {
        if (!isValidNumberId(anomaly?.alert?.id + "")) {
            return null;
        }
        let chartData = null;
        try {
            const alertEvalution = {
                alert: ({ id: anomaly?.alert?.id } as unknown) as Alert,
                start: dateRange.from.getTime(),
                end: dateRange.to.getTime(),
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
                <PageLoadingIndicator />
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
                            <TimeSeriesChartCard
                                data={chartData as AlertEvaluation}
                                title={t("label.chart")}
                            />
                        </Grid>
                    </Grid>
                )}
            </PageContents>
        </PageContainer>
    );
};
