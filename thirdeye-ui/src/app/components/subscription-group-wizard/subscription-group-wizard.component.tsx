import {
    Box,
    Button,
    Grid,
    Step,
    StepLabel,
    Stepper,
    Typography,
} from "@material-ui/core";
import { isEmpty, kebabCase } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";
import { createEmptySubscriptionGroup } from "../../utils/subscription-group-util/subscription-group-util";
import { LoadingIndicator } from "../loading-indicator/loading-indicator.component";
import { TransferList } from "../transfer-list/transfer-list.component";
import { SubscriptionGroupPropertiesForm } from "./subscription-group-properties-form/subscription-group-properties-form.component";
import {
    SubscriptionGroupWizardProps,
    SubscriptionGroupWizardStep,
} from "./subscription-group-wizard.interfaces";
import { useSubscriptionGroupWizardStyles } from "./subscription-group-wizard.styles";

const FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES =
    "FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES";

export const SubscriptionGroupWizard: FunctionComponent<SubscriptionGroupWizardProps> = (
    props: SubscriptionGroupWizardProps
) => {
    const subscriptionGroupWizardClasses = useSubscriptionGroupWizardStyles();
    const [loading, setLoading] = useState(false);
    const [newSubscriptionGroup, setNewSubscriptionGroup] = useState<
        SubscriptionGroup
    >(createEmptySubscriptionGroup());
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [currentStep, setCurrentStep] = useState<SubscriptionGroupWizardStep>(
        SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
    );
    const { t } = useTranslation();

    useEffect(() => {
        // Input changed, reset
        setNewSubscriptionGroup(
            props.subscriptionGroup || createEmptySubscriptionGroup()
        );
        setCurrentStep(
            SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
        );
    }, [props.subscriptionGroup]);

    useEffect(() => {
        initStep();

        // Notify
        props.onChange && props.onChange(currentStep);
    }, [currentStep]);

    const onSubscriptionGroupPropertiesFormSubmit = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        // Update subscription group with form inputs
        setNewSubscriptionGroup((newSubscriptionGroup) =>
            Object.assign(newSubscriptionGroup, subscriptionGroup)
        );

        // Next step
        onNext();
    };

    const onSubscriptionGroupAlertsChange = (alerts: Alert[]): void => {
        setNewSubscriptionGroup(
            (newSubscriptionGroup): SubscriptionGroup => {
                newSubscriptionGroup.alerts = alerts;

                return newSubscriptionGroup;
            }
        );
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    const onBack = (): void => {
        if (
            currentStep ===
            SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
        ) {
            // Already on first step
            return;
        }

        // Previous step
        setCurrentStep(
            SubscriptionGroupWizardStep[
                SubscriptionGroupWizardStep[
                    currentStep - 1
                ] as keyof typeof SubscriptionGroupWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (currentStep === SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            props.onFinish && props.onFinish(newSubscriptionGroup);

            return;
        }

        // Next step
        setCurrentStep(
            SubscriptionGroupWizardStep[
                SubscriptionGroupWizardStep[
                    currentStep + 1
                ] as keyof typeof SubscriptionGroupWizardStep
            ]
        );
    };

    const initStep = (): void => {
        switch (currentStep) {
            case SubscriptionGroupWizardStep.SUBSCRIBE_ALERTS: {
                if (!isEmpty(alerts)) {
                    // Alerts already received
                    break;
                }

                // Get alerts to subscribe to
                setLoading(true);

                props.getAlerts &&
                    props
                        .getAlerts()
                        .then((receivedAlerts: Alert[]): void => {
                            setAlerts(receivedAlerts);
                        })
                        .finally((): void => {
                            setLoading(false);
                        });

                break;
            }
            case SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT: {
                console.log(newSubscriptionGroup);

                break;
            }
        }
    };

    const transferListGetKey = (alert: Alert): number => {
        if (!alert) {
            return -1;
        }

        return alert.id;
    };

    const transferListRenderer = (alert: Alert): string => {
        if (!alert) {
            return "";
        }

        return alert.name;
    };

    return (
        <>
            <Grid container>
                {/* Stepper */}
                <Grid item md={12}>
                    <Stepper alternativeLabel activeStep={currentStep}>
                        {Object.values(SubscriptionGroupWizardStep)
                            .filter(
                                (subscriptionGroupCreateUpdateWizardStep) =>
                                    typeof subscriptionGroupCreateUpdateWizardStep ===
                                    "string"
                            )
                            .map(
                                (
                                    subscriptionGroupCreateUpdateWizardStep,
                                    index
                                ) => (
                                    <Step key={index}>
                                        <StepLabel>
                                            {t(
                                                `label.${kebabCase(
                                                    subscriptionGroupCreateUpdateWizardStep as string
                                                )}`
                                            )}
                                        </StepLabel>
                                    </Step>
                                )
                            )}
                    </Stepper>
                </Grid>

                {/* Subscription group properties */}
                {currentStep ===
                    SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES && (
                    <>
                        {/* Step label */}
                        <Grid item md={12}>
                            <Typography variant="h4">
                                {t("label.subscription-group-properties")}
                            </Typography>
                        </Grid>

                        <Grid item md={12} />

                        {/* Subscription group properties form */}
                        <Grid item md={12}>
                            <SubscriptionGroupPropertiesForm
                                id={FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES}
                                subscriptionGroup={newSubscriptionGroup}
                                onSubmit={
                                    onSubscriptionGroupPropertiesFormSubmit
                                }
                            />
                        </Grid>
                    </>
                )}

                {/* Subscribe alerts */}
                {currentStep === SubscriptionGroupWizardStep.SUBSCRIBE_ALERTS &&
                    !loading && (
                        <>
                            {/* Step label */}
                            <Grid item md={12}>
                                <Typography variant="h4">
                                    {t("label.subscribe-alerts")}
                                </Typography>
                            </Grid>

                            <Grid item md={12} />

                            <Grid item md={12}>
                                <TransferList<Alert>
                                    fromLabel={t("label.all-alerts")}
                                    fromList={alerts}
                                    listItemKeyFn={transferListGetKey}
                                    listItemTextFn={transferListRenderer}
                                    toLabel={t("label.subscribed-alerts")}
                                    toList={newSubscriptionGroup.alerts}
                                    onChange={onSubscriptionGroupAlertsChange}
                                />
                            </Grid>
                        </>
                    )}

                {/* Review and submit */}
                {currentStep ===
                    SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT && (
                    <>
                        {/* Step label */}
                        <Grid item md={12}>
                            <Typography variant="h4">
                                {t("label.review-and-submit")}
                            </Typography>
                        </Grid>

                        <Grid item md={12} />

                        <Grid item md={12}>
                            <Grid container justify="flex-end">
                                <Grid item md={2}>
                                    <Typography variant="body1">
                                        <strong>{t("label.name")}</strong>
                                    </Typography>
                                </Grid>

                                <Grid item md={10}>
                                    <Typography variant="body1">
                                        {newSubscriptionGroup.name}
                                    </Typography>
                                </Grid>

                                <Grid item md={2}>
                                    <Typography variant="body1">
                                        <strong>
                                            {t("label.subscribed-alerts")}
                                        </strong>
                                    </Typography>
                                </Grid>

                                {isEmpty(newSubscriptionGroup.alerts) && (
                                    <Grid item md={10}>
                                        <Typography variant="body1">
                                            {t("label.none")}
                                        </Typography>
                                    </Grid>
                                )}

                                {!isEmpty(newSubscriptionGroup.alerts) && (
                                    <Grid item md={10}>
                                        {newSubscriptionGroup.alerts.map(
                                            (alert, index) => (
                                                <Typography
                                                    key={index}
                                                    variant="body1"
                                                >
                                                    {alert.name}
                                                </Typography>
                                            )
                                        )}
                                    </Grid>
                                )}
                            </Grid>
                        </Grid>
                    </>
                )}
            </Grid>

            {loading && <LoadingIndicator />}

            <Box padding={2} />

            {/* Controls */}
            <Grid
                container
                alignItems="stretch"
                className={subscriptionGroupWizardClasses.controlsContainer}
                direction="column"
                justify="flex-end"
            >
                {/* Border for controls container */}
                <Grid item>
                    <Box
                        border={Dimension.WIDTH_BORDER_DEFAULT}
                        borderBottom={0}
                        borderColor={Palette.COLOR_BORDER_DEFAULT}
                        borderLeft={0}
                        borderRight={0}
                    />
                </Grid>

                <Grid item>
                    <Grid container justify="space-between">
                        {/* Cancel */}
                        <Grid item>
                            {props.showCancel && (
                                <Button
                                    color="primary"
                                    size="large"
                                    variant="outlined"
                                    onClick={onCancel}
                                >
                                    {t("label.cancel")}
                                </Button>
                            )}
                        </Grid>

                        <Grid item>
                            <Grid container>
                                {/* Back */}
                                <Grid item>
                                    <Button
                                        color="primary"
                                        disabled={
                                            currentStep ===
                                            SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
                                        }
                                        size="large"
                                        variant="outlined"
                                        onClick={onBack}
                                    >
                                        {t("label.back")}
                                    </Button>
                                </Grid>

                                {/* Next */}
                                <Grid item>
                                    {/* Subscription group properties form submit button for first
                                    step */}
                                    {currentStep ===
                                        SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES && (
                                        <Button
                                            color="primary"
                                            form={
                                                FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES
                                            }
                                            size="large"
                                            type="submit"
                                            variant="contained"
                                        >
                                            {t("label.next")}
                                        </Button>
                                    )}

                                    {/* Next button for all other steps */}
                                    {currentStep !==
                                        SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES && (
                                        <Button
                                            color="primary"
                                            size="large"
                                            variant="contained"
                                            onClick={onNext}
                                        >
                                            {currentStep ===
                                            SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT
                                                ? t("label.finish")
                                                : t("label.next")}
                                        </Button>
                                    )}
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </>
    );
};
