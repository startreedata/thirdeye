import {
    Box,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Snackbar,
    Typography,
} from "@material-ui/core";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import { Alert as CustomAlert } from "@material-ui/lab";
import yaml from "js-yaml";
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
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import alertPreview from "../../utils/defaults/alert-preview";
import DETECTION_CONFIG from "../../utils/defaults/detection-config";
import {
    ApplicationRoute,
    getAlertsCreatePath,
} from "../../utils/route/routes-util";

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
        const [subscriptionGroup, setSubscriptionGroup] = useState("");
        const [activeStep, setActiveStep] = useState(0);
        const [message, setMessage] = useState<
            { status: "success" | "error"; text: string } | undefined
        >();
        const [previewData, setPreviewData] = useState<AlertEvaluation>();
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
                await createAlert(yaml.safeLoad(detectionConfig) as Alert);
                setMessage({
                    status: "success",
                    text: "Alert created successfully",
                });
                props.history.push(ApplicationRoute.ALERTS_ALL);
            } catch (err) {
                console.error(err);
                setMessage({
                    status: "error",
                    text: "Failed to create an alert",
                });
            } finally {
                setLoading(false);
            }
        };

        const handlePreviewAlert = async (): Promise<void> => {
            try {
                const preview = await getAlertEvaluation(
                    (alertPreview as unknown) as AlertEvaluation
                );
                setPreviewData(preview);
            } catch (err) {
                console.error(err);
                setPreviewData((alertPreview as unknown) as AlertEvaluation);
            }
        };

        const selectionGroupComp = (
            <FormControl
                style={{ margin: "8px 0", minWidth: 250, borderRadius: 8 }}
                variant="outlined"
            >
                <InputLabel id="select-group">
                    Add to Subscription Group
                </InputLabel>
                <Select
                    label="Add to Subscription Group"
                    labelId="select-group"
                    style={{ borderRadius: 8 }}
                    value={subscriptionGroup}
                    onChange={(
                        e: React.ChangeEvent<{
                            name?: string;
                            value: unknown;
                        }>
                    ): void => setSubscriptionGroup(e.target.value + "")}
                >
                    <MenuItem value="1">1st</MenuItem>
                    <MenuItem value="2">2nd</MenuItem>
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
                                        label: "Detection Configuration",
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
                                                previewData={previewData}
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
                                        label: "Subscription Configuration",
                                        // eslint-disable-next-line react/display-name
                                        content: (
                                            <ConfigurationStep
                                                config={subscriptionConfig}
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
                                        label: "Review & Submit",
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
                                                    subscriptionGroup
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
                    {
                        <Snackbar
                            autoHideDuration={3000}
                            open={!!message}
                            onClose={(): void => setMessage(undefined)}
                        >
                            {message && (
                                <CustomAlert
                                    severity={message?.status}
                                    onClose={(): void => setMessage(undefined)}
                                >
                                    {message?.text}
                                </CustomAlert>
                            )}
                        </Snackbar>
                    }
                </PageContents>
            </PageContainer>
        );
    }
);
