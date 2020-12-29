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
import {
    createEmptySubscriptionGroup,
    getSubscriptionGroupAlertId,
    getSubscriptionGroupAlertName,
    getSubscriptionGroupCardData,
} from "../../utils/subscription-group-util/subscription-group-util";
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
    >(props.subscriptionGroup || createEmptySubscriptionGroup());
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [currentWizardStep, setCurrentWizardStep] = useState<
        SubscriptionGroupWizardStep
    >(SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES);
    const { t } = useTranslation();

    useEffect(() => {
        initWizardStep();

        // Notify
        props.onChange && props.onChange(currentWizardStep);
    }, [currentWizardStep]);

    const onSubmitSubscriptionGroupPropertiesForm = (
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
        // Update subscription group with subscribed alerts
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
            currentWizardStep ===
            SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
        ) {
            // Already on first step
            return;
        }

        // Calculate previous step
        setCurrentWizardStep(
            SubscriptionGroupWizardStep[
                SubscriptionGroupWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof SubscriptionGroupWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (
            currentWizardStep === SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT
        ) {
            // On last step
            props.onFinish && props.onFinish(newSubscriptionGroup);

            return;
        }

        // Calculate next step
        setCurrentWizardStep(
            SubscriptionGroupWizardStep[
                SubscriptionGroupWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof SubscriptionGroupWizardStep
            ]
        );
    };

    const initWizardStep = (): void => {
        switch (currentWizardStep) {
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
                        .then((alerts: Alert[]): void => {
                            setAlerts(alerts);

                            // Associate alerts with subscription group
                            const subscriptionGroupCardData = getSubscriptionGroupCardData(
                                newSubscriptionGroup,
                                alerts
                            );
                            onSubscriptionGroupAlertsChange(
                                subscriptionGroupCardData.alerts as Alert[]
                            );
                        })
                        .finally((): void => {
                            setLoading(false);
                        });

                break;
            }
        }
    };

    return (
        <>
            <Grid container>
                {/* Stepper */}
                <Grid item md={12}>
                    <Stepper alternativeLabel activeStep={currentWizardStep}>
                        {Object.values(SubscriptionGroupWizardStep)
                            .filter(
                                (subscriptionGroupWizardStep) =>
                                    typeof subscriptionGroupWizardStep ===
                                    "string"
                            )
                            .map((subscriptionGroupWizardStep, index) => (
                                <Step key={index}>
                                    <StepLabel>
                                        {t(
                                            `label.${kebabCase(
                                                subscriptionGroupWizardStep as string
                                            )}`
                                        )}
                                    </StepLabel>
                                </Step>
                            ))}
                    </Stepper>
                </Grid>

                {/* Subscription group properties */}
                {currentWizardStep ===
                    SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES && (
                    <>
                        {/* Step label */}
                        <Grid item md={12}>
                            <Typography variant="h5">
                                {t("label.subscription-group-properties")}
                            </Typography>
                        </Grid>

                        {/* Spacer */}
                        <Grid item md={12} />

                        {/* Subscription group properties form */}
                        <Grid item md={12}>
                            <SubscriptionGroupPropertiesForm
                                id={FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES}
                                subscriptionGroup={newSubscriptionGroup}
                                onSubmit={
                                    onSubmitSubscriptionGroupPropertiesForm
                                }
                            />
                        </Grid>
                    </>
                )}

                {/* Subscribe alerts */}
                {currentWizardStep ===
                    SubscriptionGroupWizardStep.SUBSCRIBE_ALERTS &&
                    !loading && (
                        <>
                            {/* Step label */}
                            <Grid item md={12}>
                                <Typography variant="h5">
                                    {t("label.subscribe-alerts")}
                                </Typography>
                            </Grid>

                            {/* Spacer */}
                            <Grid item md={12} />

                            <Grid item md={12}>
                                <TransferList<Alert>
                                    fromLabel={t("label.all-alerts")}
                                    fromList={alerts}
                                    listItemKeyFn={getSubscriptionGroupAlertId}
                                    listItemTextFn={
                                        getSubscriptionGroupAlertName
                                    }
                                    toLabel={t("label.subscribed-alerts")}
                                    toList={newSubscriptionGroup.alerts}
                                    onChange={onSubscriptionGroupAlertsChange}
                                />
                            </Grid>
                        </>
                    )}

                {/* Review and submit */}
                {currentWizardStep ===
                    SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT && (
                    <>
                        {/* Step label */}
                        <Grid item md={12}>
                            <Typography variant="h5">
                                {t("label.review-and-submit")}
                            </Typography>
                        </Grid>

                        {/* Spacer */}
                        <Grid item md={12} />

                        {/* Subscription group information */}
                        <Grid item md={12}>
                            <Grid container justify="flex-end">
                                {/* Name */}
                                <Grid item md={2}>
                                    <Typography variant="subtitle1">
                                        <strong>{t("label.name")}</strong>
                                    </Typography>
                                </Grid>

                                <Grid item md={10}>
                                    <Typography variant="body1">
                                        {newSubscriptionGroup.name}
                                    </Typography>
                                </Grid>

                                {/* Subscribed alerts */}
                                <Grid item md={2}>
                                    <Typography variant="subtitle1">
                                        <strong>
                                            {t("label.subscribed-alerts")}
                                        </strong>
                                    </Typography>
                                </Grid>

                                {isEmpty(newSubscriptionGroup.alerts) && (
                                    // No subscribed alerts
                                    <Grid item md={10}>
                                        <Typography variant="body1">
                                            {t(
                                                "label.no-data-available-marker"
                                            )}
                                        </Typography>
                                    </Grid>
                                )}

                                {!isEmpty(newSubscriptionGroup.alerts) && (
                                    // All subscribed alerts
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

            {/* Spacer */}
            <Box padding={2} />

            {/* Controls */}
            <Grid
                container
                alignItems="stretch"
                className={subscriptionGroupWizardClasses.controlsContainer}
                direction="column"
                justify="flex-end"
            >
                {/* Separator */}
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
                        {/* Cancel button */}
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
                                {/* Back button */}
                                <Grid item>
                                    <Button
                                        color="primary"
                                        disabled={
                                            currentWizardStep ===
                                            SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
                                        }
                                        size="large"
                                        variant="outlined"
                                        onClick={onBack}
                                    >
                                        {t("label.back")}
                                    </Button>
                                </Grid>

                                {/* Next button */}
                                <Grid item>
                                    {/* Submit button for subscription group properties form in for
                                    first step */}
                                    {currentWizardStep ===
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
                                    {currentWizardStep !==
                                        SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES && (
                                        <Button
                                            color="primary"
                                            size="large"
                                            variant="contained"
                                            onClick={onNext}
                                        >
                                            {currentWizardStep ===
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
