import {
    Box,
    FormControl,
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
import { PageContainer } from "../../components/containers/page-container.component";
import CommonCodeMirror from "../../components/editor/code-mirror.component";
import { AppLoader } from "../../components/loader/app-loader.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { CustomStepper } from "../../components/stepper/stepper.component";
import { createAlert } from "../../rest/alert/alert.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs.store";
import DETECTION_CONFIG from "../../utils/defaults/detection-config";
import { AppRoute, getAlertsCreatePath } from "../../utils/route/routes.util";

const DEFAULT_SUBSCRIPTION = "This is default subscription config";

export const AlertsCreatePage = withRouter(
    (props: RouteComponentProps): ReactElement => {
        const [loading, setLoading] = useState(false);
        const [push] = useApplicationBreadcrumbsStore((state) => [state.push]);
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
        const { t } = useTranslation();

        useEffect(() => {
            // Create page breadcrumb
            push([
                {
                    text: t("label.create"),
                    path: getAlertsCreatePath(),
                },
            ]);

            setLoading(false);
        }, [push, t]);

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
                props.history.push(AppRoute.ALERTS_ALL);
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

        return (
            <PageContainer centered noPadding>
                <AppLoader visible={loading} />
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
                                            General Details
                                        </Typography>
                                    }
                                    name={"Define Detection"}
                                    showPreviewButton={true}
                                    onConfigChange={setDetectionConfig}
                                    onResetConfig={(): void =>
                                        setDetectionConfig(DETECTION_CONFIG)
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
                                    onConfigChange={setSubscriptionConfig}
                                    onResetConfig={(): void =>
                                        setDetectionConfig(DEFAULT_SUBSCRIPTION)
                                    }
                                />
                            ),
                        },
                        {
                            label: "Review & Submit",
                            // eslint-disable-next-line react/display-name
                            content: (
                                <>
                                    <Typography variant="h4">
                                        Review & Submit
                                    </Typography>
                                    <Typography variant="h6">
                                        Detection Configuration
                                    </Typography>
                                    <CommonCodeMirror
                                        options={{
                                            mode: "text/x-ymal",
                                            readOnly: true,
                                        }}
                                        value={detectionConfig}
                                    />
                                    <Typography variant="h6">
                                        Subscription Configuration
                                    </Typography>
                                    <Typography variant="subtitle1">
                                        Add to Subscription Group
                                    </Typography>
                                    <Typography variant="body1">
                                        {subscriptionGroup}
                                    </Typography>
                                    <CommonCodeMirror
                                        options={{
                                            mode: "text/x-ymal",
                                            readOnly: true,
                                        }}
                                        value={subscriptionConfig}
                                    />
                                    <Box>
                                        <Button color="primary" variant="text">
                                            Preview Alert
                                        </Button>
                                    </Box>
                                </>
                            ),
                        },
                    ]}
                    onStepChange={handleStepChange}
                />
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
                        endIcon={activeStep >= 2 ? null : <ArrowForwardIcon />}
                        variant="contained"
                        onClick={(): void => handleStepChange(activeStep + 1)}
                    >
                        {activeStep >= 2 ? "Finish" : "Next"}
                    </Button>
                </Box>
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
            </PageContainer>
        );
    }
);
