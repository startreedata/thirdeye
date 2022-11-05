import {
    Box,
    Button,
    Grid,
    Link,
    TableCell,
    TableRow,
    Typography,
} from "@material-ui/core";
import ArrowDropDownIcon from "@material-ui/icons/ArrowDropDown";
import ArrowRightIcon from "@material-ui/icons/ArrowRight";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useInView } from "react-intersection-observer";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import {
    createAlertEvaluation,
    extractDetectionEvaluation,
} from "../../../utils/alerts/alerts.util";
import { getAlertsAlertViewPath } from "../../../utils/routes/routes.util";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Pluralize } from "../../pluralize/pluralize.component";
import { generateChartOptionsForMetricsReport } from "../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { MetricReportExpandList } from "../metrics-report-expand-list/metrics-report-expand-list.component";
import { useMetricsReportListStyles } from "../metrics-report-list.styles";
import { MetricsReportRowProps } from "./metrics-report-row.interface";

export const MetricsReportRow: FunctionComponent<MetricsReportRowProps> = ({
    anomalyAlert,
    chartStart,
    chartEnd,
}) => {
    const classes = useMetricsReportListStyles();
    // Use object destructing, so you don't need to remember the exact order
    const { ref, inView } = useInView({
        triggerOnce: true,
        delay: 150,
        threshold: 1,
    });
    const { t } = useTranslation();
    const {
        getEvaluation,
        status: evaluationRequestStatus,
        errorMessages: getEvaluationErrors,
    } = useGetEvaluation();
    const [chartOptions, setChartOptions] =
        useState<TimeSeriesChartProps | null>(null);
    const [isOpen, setIsOpen] = useState(false);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        if (inView) {
            fetchAlertEvaluation();
        }
    }, [chartStart, chartEnd, inView]);

    const fetchAlertEvaluation = (): void => {
        getEvaluation(
            createAlertEvaluation(anomalyAlert.alert, chartStart, chartEnd)
        ).then((evaluation) => {
            if (evaluation) {
                const options = generateChartOptionsForMetricsReport(
                    extractDetectionEvaluation(evaluation)[0],
                    anomalyAlert.anomalies,
                    t
                );
                options.zoom = true;
                options.brush = false;
                options.legend = false;
                options.yAxis = {
                    enabled: false,
                };
                options.margins = {
                    top: 0,
                    bottom: 10, // This needs to exist for the x axis
                    left: 0,
                    right: 0,
                };
                setChartOptions(options);
            }
        });
    };

    return (
        <>
            <TableRow
                className={isOpen ? classes.expanded : undefined}
                innerRef={ref}
                key={anomalyAlert.alert.name}
            >
                <TableCell component="th" scope="row">
                    <Grid container>
                        <Grid item sm={2} xs={3}>
                            <Button
                                variant="text"
                                onClick={() => setIsOpen(!isOpen)}
                            >
                                {!isOpen && <ArrowRightIcon />}
                                {isOpen && <ArrowDropDownIcon />}
                            </Button>
                        </Grid>
                        <Grid item sm={10} xs={9}>
                            <Box>
                                <Link
                                    component={RouterLink}
                                    to={getAlertsAlertViewPath(
                                        anomalyAlert.alert.id
                                    )}
                                >
                                    {anomalyAlert.alert.name}
                                </Link>
                            </Box>
                            <Box>
                                <Typography
                                    color="textSecondary"
                                    variant="caption"
                                >
                                    {!isOpen &&
                                        anomalyAlert.anomalies.length === 1 &&
                                        t(
                                            "message.show-count-entity-for-time-range",
                                            {
                                                count: anomalyAlert.anomalies
                                                    .length,
                                                entity: t("label.anomaly"),
                                            }
                                        )}
                                    {!isOpen &&
                                        anomalyAlert.anomalies.length > 1 &&
                                        t(
                                            "message.show-count-entity-for-time-range",
                                            {
                                                count: anomalyAlert.anomalies
                                                    .length,
                                                entity: t("label.anomalies"),
                                            }
                                        )}
                                    {isOpen && (
                                        <Pluralize
                                            count={
                                                anomalyAlert.anomalies.length
                                            }
                                            plural={t("label.anomalies")}
                                            singular={t("label.anomaly")}
                                        />
                                    )}
                                </Typography>
                            </Box>
                        </Grid>
                    </Grid>
                </TableCell>
                <TableCell>{anomalyAlert.metric}</TableCell>
                <TableCell>{anomalyAlert.dataset}</TableCell>
                <TableCell>
                    <LoadingErrorStateSwitch
                        errorState={
                            getEvaluationErrors &&
                            getEvaluationErrors.length > 0 && (
                                <Typography color="error" variant="caption">
                                    {getEvaluationErrors[0]}
                                </Typography>
                            )
                        }
                        isError={evaluationRequestStatus === ActionStatus.Error}
                        isLoading={
                            evaluationRequestStatus === ActionStatus.Working
                        }
                        loadingState={<SkeletonV1 animation="pulse" />}
                    >
                        {chartOptions && (
                            <Box minWidth={500}>
                                <TimeSeriesChart
                                    height={100}
                                    {...chartOptions}
                                />
                            </Box>
                        )}
                    </LoadingErrorStateSwitch>
                </TableCell>
            </TableRow>
            {isOpen && (
                <TableRow>
                    <TableCell colSpan={10}>
                        <MetricReportExpandList
                            anomalies={anomalyAlert.anomalies}
                        />
                    </TableCell>
                </TableRow>
            )}
        </>
    );
};
