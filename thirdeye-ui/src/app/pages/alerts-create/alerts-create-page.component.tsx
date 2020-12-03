import {
    Box,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Typography,
} from "@material-ui/core";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import yaml from "js-yaml";
import _ from "lodash";
import { useSnackbar } from "notistack";
import React, { ReactElement, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { RouteComponentProps, withRouter } from "react-router-dom";
import { Button } from "../../components/button/button.component";
import { ConfigurationStep } from "../../components/configuration-step/configuration-step.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { ReviewStep } from "../../components/review-step/review-step.component";
import { CustomStepper } from "../../components/stepper/stepper.component";
import { createAlert, getAlertEvaluation } from "../../rest/alert/alert-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    getAllSubscriptionGroups,
    updateSubscriptionGroup,
} from "../../rest/subscription-group/subscription-group-rest";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { useDateRangePickerStore } from "../../store/date-range-picker/date-range-picker-store";
import DETECTION_CONFIG from "../../utils/defaults/detection-config";
import {
    ApplicationRoute,
    getAlertsCreatePath,
} from "../../utils/route/routes-util";
import { SnackbarOption } from "../../utils/snackbar/snackbar-util";

const DEFAULT_SUBSCRIPTION = "This is default subscription config";

export const AlertsCreatePage = withRouter(
    (props: RouteComponentProps): ReactElement => {
        const [loading, setLoading] = useState(false);
        const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
            state.setPageBreadcrumbs,
        ]);
        const [detectionConfig, setDetectionConfig] = useState(
            DETECTION_CONFIG
        );
        const [subscriptionConfig, setSubscriptionConfig] = useState(
            DEFAULT_SUBSCRIPTION
        );
        const [subscriptionGroup, setSubscriptionGroup] = useState(-1);
        const [activeStep, setActiveStep] = useState(0);
        const { enqueueSnackbar } = useSnackbar();
        const [chartData, setChartData] = useState<AlertEvaluation | null>();
        const [subscriptionGroups, setSubscriptionGroups] = useState<
            SubscriptionGroup[]
        >([]);

        const [isFirstTime, setIsFirstTime] = useState(true);

        const [dateRange] = useDateRangePickerStore((state) => [
            state.dateRange,
        ]);

        const { t } = useTranslation();

        useEffect(() => {
            // Create page breadcrumbs
            setPageBreadcrumbs([
                {
                    text: t("label.create"),
                    path: getAlertsCreatePath(),
                },
            ]);

            setLoading(false);
        }, [setPageBreadcrumbs, t]);

        useEffect(() => {
            async function fetchData(): Promise<void> {
                setSubscriptionGroups(await getAllSubscriptionGroups());
            }
            fetchData();
        }, []);

        const handleStepChange = (step: number): void => {
            if (step > 2) {
                handleCreateAlert();
            } else {
                setActiveStep(step < 0 ? 0 : step > 2 ? 2 : step);
            }
        };

        const handleCreateAlert = async (): Promise<void> => {
            setLoading(true);
            try {
                const alert = await createAlert(
                    yaml.safeLoad(detectionConfig) as Alert
                );

                if (subscriptionGroup !== -1) {
                    const updatedScubscriptionGroup = subscriptionGroups.find(
                        (sg) => sg.id === subscriptionGroup
                    );
                    try {
                        await updateSubscriptionGroup({
                            ...(updatedScubscriptionGroup as SubscriptionGroup),
                            alerts: [
                                ...(updatedScubscriptionGroup?.alerts || []),
                                { id: alert.id },
                            ] as Alert[],
                        });
                    } catch (e) {
                        console.error(e);
                    }
                }
                enqueueSnackbar(
                    t("message.alert-created"),
                    SnackbarOption.SUCCESS
                );
                props.history.push(ApplicationRoute.ALERTS_ALL);
            } catch (err) {
                console.log(err);
                enqueueSnackbar(
                    _.get(err, t("message.alert-creation-failed")),
                    SnackbarOption.SUCCESS
                );
            } finally {
                setLoading(false);
            }
        };

        useEffect(() => {
            handlePreviewAlert();
        }, [dateRange]);

        const handlePreviewAlert = async (): Promise<void> => {
            if (isFirstTime) {
                setIsFirstTime(false);

                return;
            }
            setChartData(null);
            setChartData(await fetchChartData());
        };

        const fetchChartData = async (): Promise<AlertEvaluation | null> => {
            const alertEvalution = {
                alert: yaml.safeLoad(detectionConfig) as Alert,
                start: dateRange.from.getTime(),
                end: dateRange.to.getTime(),
            };

            let chartData = null;
            try {
                chartData = await getAlertEvaluation(
                    alertEvalution as AlertEvaluation
                );
            } catch (error) {
                enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
                chartData = {} as AlertEvaluation;
            }

            return chartData;
        };

        const selectionGroupComp = (
            <FormControl
                style={{ margin: "8px 0", minWidth: 250, borderRadius: 8 }}
                variant="outlined"
            >
                <InputLabel id="select-group">
                    {t("label.add-subscription-gorup")}
                </InputLabel>
                <Select
                    label={t("label.add-subscription-gorup")}
                    labelId="select-group"
                    style={{ borderRadius: 8 }}
                    value={subscriptionGroup}
                    onChange={(
                        e: React.ChangeEvent<{
                            name?: string;
                            value: unknown;
                        }>
                    ): void => setSubscriptionGroup(e.target.value as number)}
                >
                    <MenuItem key={-1} value={-1}>
                        {t("label.create-subscription-gorup")}
                    </MenuItem>
                    {subscriptionGroups.map(({ id, name }) => (
                        <MenuItem key={id} value={id}>
                            {name}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>
        );

        if (loading) {
            return (
                <PageContainer>
                    <PageLoadingIndicator />
                </PageContainer>
            );
        }

        if (loading) {
            return <PageLoadingIndicator />;
        }

        return (
            <PageContainer>
                <PageContents centerAlign hideTimeRange title="">
                    <Grid container>
                        <Grid item xs={12}>
                            <CustomStepper
                                currentStep={activeStep}
                                steps={[
                                    {
                                        label: t(
                                            "label.detection-configuration"
                                        ),
                                        // eslint-disable-next-line react/display-name
                                        content: (
                                            <ConfigurationStep
                                                config={detectionConfig}
                                                extraFields={
                                                    <Typography variant="h6">
                                                        {t(
                                                            "label.general-details"
                                                        )}
                                                    </Typography>
                                                }
                                                name={"Define Detection"}
                                                previewData={
                                                    chartData as AlertEvaluation
                                                }
                                                showPreviewButton={true}
                                                onConfigChange={
                                                    setDetectionConfig
                                                }
                                                onPreviewAlert={
                                                    handlePreviewAlert
                                                }
                                                onResetConfig={(): void =>
                                                    setDetectionConfig(
                                                        DETECTION_CONFIG
                                                    )
                                                }
                                            />
                                        ),
                                    },
                                    {
                                        label: t(
                                            "label.subscription-configuration"
                                        ),
                                        // eslint-disable-next-line react/display-name
                                        content: (
                                            <ConfigurationStep
                                                config={subscriptionConfig}
                                                editorProps={{
                                                    options: {
                                                        readOnly:
                                                            subscriptionGroup !==
                                                            -1,
                                                    },
                                                }}
                                                extraFields={selectionGroupComp}
                                                name={"Subscription"}
                                                onConfigChange={
                                                    setSubscriptionConfig
                                                }
                                                onResetConfig={(): void =>
                                                    setDetectionConfig(
                                                        DEFAULT_SUBSCRIPTION
                                                    )
                                                }
                                            />
                                        ),
                                    },
                                    {
                                        label: t("label.review-submit"),
                                        // eslint-disable-next-line react/display-name
                                        content: (
                                            <ReviewStep
                                                detectionConfig={
                                                    detectionConfig
                                                }
                                                subscriptionConfig={
                                                    subscriptionConfig
                                                }
                                                subscriptionGroup={
                                                    subscriptionGroup !== -1
                                                        ? subscriptionGroups.find(
                                                              (sg) =>
                                                                  sg.id ===
                                                                  subscriptionGroup
                                                          )?.name || ""
                                                        : "-"
                                                }
                                            />
                                        ),
                                    },
                                ]}
                                onStepChange={handleStepChange}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Box>
                                {activeStep !== 0 && (
                                    <Button
                                        color="primary"
                                        startIcon={<ArrowBackIcon />}
                                        style={{ marginRight: 10 }}
                                        variant="outlined"
                                        onClick={(): void =>
                                            handleStepChange(activeStep - 1)
                                        }
                                    >
                                        Prev
                                    </Button>
                                )}
                                <Button
                                    color="primary"
                                    endIcon={
                                        activeStep >= 2 ? null : (
                                            <ArrowForwardIcon />
                                        )
                                    }
                                    variant="contained"
                                    onClick={(): void =>
                                        handleStepChange(activeStep + 1)
                                    }
                                >
                                    {activeStep >= 2 ? "Finish" : "Next"}
                                </Button>
                            </Box>
                        </Grid>
                    </Grid>
                </PageContents>
            </PageContainer>
        );
    }
);
