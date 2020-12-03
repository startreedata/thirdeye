import { Grid } from "@material-ui/core";
import { cloneDeep, isEmpty, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AlertCard } from "../../components/alert-card/alert-card.component";
import { AlertCardData } from "../../components/alert-card/alert-card.interfaces";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { TimeSeriesChartCard } from "../../components/timeseries-chart-card/timeseries-chart-card.component";
import {
    getAlert,
    getAlertEvaluation,
    updateAlert,
} from "../../rest/alert/alert-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-group/subscription-group-rest";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { useDateRangePickerStore } from "../../store/date-range-picker/date-range-picker-store";
import { getAlertCardData } from "../../utils/alert/alert-util";
import { isValidNumberId } from "../../utils/params/params-util";
import { getAlertsDetailPath } from "../../utils/routes/routes-util";
import { SnackbarOption } from "../../utils/snackbar/snackbar-util";
import { AlertsDetailPageParams } from "./alerts-detail-page.interfaces";

export const AlertsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [alert, setAlert] = useState<AlertCardData>({} as AlertCardData);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const params = useParams<AlertsDetailPageParams>();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    const [dateRange] = useDateRangePickerStore((state) => [state.dateRange]);
    const [chartData, setChartData] = useState<AlertEvaluation | null>(
        {} as AlertEvaluation
    );

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
                    entity: t("label.alert"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            return;
        }

        let alert = {} as AlertCardData;
        const [
            alertResponse,
            subscriptionGroupsResponse,
        ] = await Promise.allSettled([
            getAlert(toNumber(params.id)),
            getAllSubscriptionGroups(),
        ]);

        if (
            alertResponse.status === "rejected" ||
            subscriptionGroupsResponse.status === "rejected"
        ) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } else {
            alert = getAlertCardData(
                alertResponse.value,
                subscriptionGroupsResponse.value
            );

            // Create page breadcrumbs
            setPageBreadcrumbs([
                {
                    text: alert.name,
                    path: getAlertsDetailPath(alert.id),
                },
            ]);
        }

        setAlert(alert);
    };

    // To fetch chartData on dateRange Change
    useEffect(() => {
        const init = async (): Promise<void> => {
            setChartData(null);

            setChartData(await fetchChartData());
        };

        init();
    }, [dateRange, params.id]);

    const fetchChartData = async (): Promise<AlertEvaluation | null> => {
        if (!isValidNumberId(params.id)) {
            return null;
        }

        let chartData = null;

        try {
            const alertEvalution = {
                alert: ({ id: params.id } as unknown) as Alert,
                start: dateRange.from.getTime(),
                end: dateRange.to.getTime(),
            } as AlertEvaluation;

            chartData = await getAlertEvaluation(alertEvalution);
        } catch (e) {
            // Empty block
            chartData = {} as AlertEvaluation;
        }

        return chartData;
    };

    const onAlertStateToggle = async (
        alertCardData: AlertCardData
    ): Promise<void> => {
        let alertCopy = cloneDeep(alertCardData.alert);
        alertCopy.active = !alertCopy.active;

        try {
            alertCopy = await updateAlert(alertCopy);

            fetchData();
        } catch (error) {
            enqueueSnackbar(
                t("message.update-error", { entity: t("label.alert") }),
                SnackbarOption.ERROR
            );
        }
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
            <PageContents centerAlign title={alert.name ? alert.name : ""}>
                {!isEmpty(alert) && (
                    <Grid container>
                        <Grid item md={12}>
                            <AlertCard
                                hideViewDetailsLinks
                                alert={alert}
                                onAlertStateToggle={onAlertStateToggle}
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
