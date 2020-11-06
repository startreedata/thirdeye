import {
    Box,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    Snackbar,
    Typography,
} from "@material-ui/core";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import { Alert } from "@material-ui/lab";
import React, { ReactElement, useState } from "react";
import { RouteComponentProps, withRouter } from "react-router-dom";
import { CustomBreadcrumbs } from "../../components/breadcrumbs/breadcrumbs.component";
import { Button } from "../../components/button/button.component";
import { ConfigurationStep } from "../../components/configuration-step/configuration-step.component";
import { PageContainer } from "../../components/containers/page-container.component";
import CommonCodeMirror from "../../components/editor/code-mirror.component";
import { AppLoader } from "../../components/loader/app-loader.component";
import { RouterLink } from "../../components/router-link/router-link.component";
import { CustomStepper } from "../../components/stepper/stepper.component";
import { DETECTION_CONFIG } from "../../mock";
import { createAlert } from "../../utils/rest/alerts-rest/alerts-rest.util";
import { AppRoute } from "../../utils/routes.util";

const DEFAULT_SUBSCRIPTION = "This is default subscription config";

export const CreateAlertPage = withRouter(
    (props: RouteComponentProps): ReactElement => {
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
        const [loading, setLoading] = useState(false);

        const handleNext = (step: number): void => {
            if (step > 2) {
                handleCreateAlert();
            } else {
                setActiveStep(step < 0 ? 0 : step > 2 ? 2 : step);
            }
        };

        const handleCreateAlert = async (): Promise<void> => {
            setLoading(true);
            try {
                await createAlert(detectionConfig);
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

        const breadcrumbs = (
            <CustomBreadcrumbs>
                <RouterLink to={AppRoute.ALERTS_ALL}>Alerts</RouterLink>
                <Typography>New Alert</Typography>
            </CustomBreadcrumbs>
        );

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

        return (
            <PageContainer centered noPadding breadcrumbs={breadcrumbs}>
                <AppLoader visible={loading} />
                <CustomStepper
                    clickable={true}
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
                    onStepChange={handleNext}
                />
                <Box>
                    <Button
                        color="primary"
                        startIcon={
                            activeStep === 2 ? <ArrowForwardIcon /> : null
                        }
                        variant="contained"
                        onClick={(): void => handleNext(activeStep + 1)}
                    >
                        {activeStep === 2 ? "Finish" : "Next"}
                    </Button>
                </Box>

                {
                    <Snackbar
                        autoHideDuration={3000}
                        open={!!message}
                        onClose={(): void => setMessage(undefined)}
                    >
                        {message && (
                            <Alert
                                severity={message?.status}
                                onClose={(): void => setMessage(undefined)}
                            >
                                {message?.text}
                            </Alert>
                        )}
                    </Snackbar>
                }
            </PageContainer>
        );
    }
);
