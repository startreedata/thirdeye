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
import { useHistory } from "react-router-dom";
import { Button } from "../../components/button/button.component";
import { ConfigurationStep } from "../../components/configuration-step/configuration-step.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { ReviewStep } from "../../components/review-step/review-step.component";
import { CustomStepper } from "../../components/stepper/stepper.component";
import {
    createAlert,
    getAlertEvaluation,
} from "../../rest/alerts-rest/alerts-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    getAllSubscriptionGroups,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups-rest/subscription-groups-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import DETECTION_CONFIG from "../../utils/defaults/detection-config";
import {
    AppRoute,
    getAlertsCreatePath,
} from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";

const DEFAULT_SUBSCRIPTION = "This is default subscription config";

export const AlertsCreatePage = (): ReactElement => {
    const [loading, setLoading] = useState(false);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [detectionConfig, setDetectionConfig] = useState(DETECTION_CONFIG);
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
    const history = useHistory();

    const [isFirstTime, setIsFirstTime] = useState(true);

    const [appTimeRangeDuration] = useAppTimeRangeStore((state) => [
        state.appTimeRangeDuration,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.create"),
                pathFn: getAlertsCreatePath,
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
                yaml.load(detectionConfig) as Alert
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
                t("message.create-success", { entity: t("label.alert") }),
                getSuccessSnackbarOption()
            );
            history.push(AppRoute.ALERTS_ALL);
        } catch (err) {
            console.log(err);
            enqueueSnackbar(
                _.get(
                    err,
                    t("message.create-error", { entity: t("label.alert") })
                ),
                getErrorSnackbarOption()
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        handlePreviewAlert();
    }, [appTimeRangeDuration]);

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
            alert: yaml.load(detectionConfig) as Alert,
            start: appTimeRangeDuration.startTime,
            end: appTimeRangeDuration.endTime,
        };

        let chartData = null;
        try {
            chartData = await getAlertEvaluation(
                alertEvalution as AlertEvaluation
            );
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), getErrorSnackbarOption());
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
                <LoadingIndicator />
            </PageContainer>
        );
    }

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContainer>
            <PageContents centered hideTimeRange title="">
                <Grid container>
                    <Grid item xs={12}>
                        <CustomStepper
                            currentStep={activeStep}
                            steps={[
                                {
                                    label: t("label.detection-configuration"),
                                    // eslint-disable-next-line react/display-name
                                    content: (
                                        <ConfigurationStep
                                            config={detectionConfig}
                                            extraFields={
                                                <Typography variant="h6">
                                                    {t("label.general-details")}
                                                </Typography>
                                            }
                                            name={"Define Detection"}
                                            previewData={
                                                chartData as AlertEvaluation
                                            }
                                            showPreviewButton={true}
                                            onConfigChange={setDetectionConfig}
                                            onPreviewAlert={handlePreviewAlert}
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
                                            detectionConfig={detectionConfig}
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
};
