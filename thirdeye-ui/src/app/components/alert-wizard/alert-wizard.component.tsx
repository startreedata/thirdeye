import { Box, Button, Grid, Typography } from "@material-ui/core";
import { Alert as MuiAlert } from "@material-ui/lab";
import {
    AppLoadingIndicatorV1,
    JSONEditorV1,
    PageContentsCardV1,
    StepperV1,
} from "@startree-ui/platform-ui";
import { cloneDeep, isEmpty, kebabCase, xor } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createDefaultAlert,
    getUiAlert,
    omitNonUpdatableData,
} from "../../utils/alerts/alerts.util";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { validateJSON } from "../../utils/validation/validation.util";
import { SubscriptionGroupWizard } from "../subscription-group-wizard/subscription-group-wizard.component";
import { SubscriptionGroupWizardStep } from "../subscription-group-wizard/subscription-group-wizard.interfaces";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TransferList } from "../transfer-list/transfer-list.component";
import { AlertEvaluationTimeSeriesCard } from "../visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { AlertWizardProps, AlertWizardStep } from "./alert-wizard.interfaces";
import { useAlertWizardStyles } from "./alert-wizard.styles";

export const AlertWizard: FunctionComponent<AlertWizardProps> = (
    props: AlertWizardProps
) => {
    const alertWizardClasses = useAlertWizardStyles();
    const [loading, setLoading] = useState(true);
    const editableAlert = props.alert
        ? omitNonUpdatableData(props.alert)
        : undefined;
    const [newAlert, setNewAlert] = useState<Alert>(
        editableAlert || createDefaultAlert()
    );
    const [newAlertJSON, setNewAlertJSON] = useState(
        JSON.stringify(editableAlert || createDefaultAlert())
    );
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [
        detectionConfigurationError,
        setDetectionConfigurationError,
    ] = useState(false);
    const [
        detectionConfigurationHelperText,
        setDetectionConfigurationHelperText,
    ] = useState("");
    const [subs, setSubs] = useState<SubscriptionGroup[]>([]);
    const [initialSelectedSubs, setInitialSelectedSubs] = useState<
        SubscriptionGroup[]
    >([]);
    const [selecteddSubs, setSelectedSubs] = useState<SubscriptionGroup[]>([]);
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const [currentWizardStep, setCurrentWizardStep] = useState<AlertWizardStep>(
        AlertWizardStep.DETECTION_CONFIGURATION
    );
    const [wizard, setWizard] = useState("");
    const { timeRangeDuration } = useTimeRange();
    const { t } = useTranslation();

    useEffect(() => {
        initSubs();
    }, [subs]);

    useEffect(() => {
        refreshAlertEvaluation();
    }, [timeRangeDuration]);

    useEffect(() => {
        if (currentWizardStep === AlertWizardStep.SUBSCRIPTION_GROUPS) {
            props.getAllSubscriptionGroups &&
                props
                    .getAllSubscriptionGroups()
                    .then((subs: SubscriptionGroup[]): void => {
                        setSubs(subs);
                    });
        }
    }, [currentWizardStep]);

    const refreshAlertEvaluation = (): void => {
        setAlertEvaluation({} as AlertEvaluation);
        if (validateDetectionConfiguration()) {
            setAlertEvaluation(null);
            let fetchedAlertEvaluation = {} as AlertEvaluation;
            props.getAlertEvaluation &&
                props
                    .getAlertEvaluation(JSON.parse(newAlertJSON))
                    .then((alertEvaluation: AlertEvaluation): void => {
                        fetchedAlertEvaluation = alertEvaluation;
                    })
                    .finally((): void => {
                        setAlertEvaluation(fetchedAlertEvaluation);
                    });
        }
    };

    const onDetectionConfigurationChange = (value: string): void => {
        setNewAlertJSON(value);
        setAlertEvaluation({} as AlertEvaluation);
    };

    const onCreateNew = (): void => {
        setLoading(true);
        props.getAllAlerts().then((alerts: Alert[]): void => {
            setAlerts(alerts);
            setWizard("sub");
        });
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    const onBack = (): void => {
        if (currentWizardStep === AlertWizardStep.DETECTION_CONFIGURATION) {
            // Already on first step
            return;
        }

        // Determine previous step
        setCurrentWizardStep(
            AlertWizardStep[
                AlertWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof AlertWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (
            currentWizardStep === AlertWizardStep.DETECTION_CONFIGURATION &&
            !validateDetectionConfiguration()
        ) {
            return;
        }

        if (currentWizardStep === AlertWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            if (props.alert) {
                // Edit Alert
                const selectedSubscritpionGroups = [];
                const omittedSubscriptionGroups = [];

                // Find updated subscriptionGroups
                const subscriptionGroupsToBeUpdated = xor(
                    initialSelectedSubs,
                    selecteddSubs
                );

                // Check if subscriptionGroup added or removed
                for (const subscritionGroup of subscriptionGroupsToBeUpdated) {
                    if (initialSelectedSubs.includes(subscritionGroup)) {
                        omittedSubscriptionGroups.push(subscritionGroup);
                    } else {
                        selectedSubscritpionGroups.push(subscritionGroup);
                    }
                }
                props.onFinish &&
                    props.onFinish(
                        newAlert,
                        selectedSubscritpionGroups,
                        omittedSubscriptionGroups
                    );
            } else {
                // Create Alert
                props.onFinish && props.onFinish(newAlert, selecteddSubs);
            }

            return;
        }

        // Determine next step
        setCurrentWizardStep(
            AlertWizardStep[
                AlertWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof AlertWizardStep
            ]
        );
    };

    const initSubs = (): void => {
        if (!props.alert) {
            setLoading(false);

            return;
        }

        const alertcard = getUiAlert(props.alert, subs);
        if (isEmpty(alertcard.subscriptionGroups)) {
            // No groups sub
            setLoading(false);

            return;
        }

        const selsubs: SubscriptionGroup[] = [];
        for (const sub of alertcard.subscriptionGroups) {
            for (const group of subs) {
                if (sub.id === group.id) {
                    // Duplicates?
                    selsubs.push(group);

                    break;
                }
            }
        }

        setSelectedSubs(selsubs);
        setInitialSelectedSubs(selsubs);
        setLoading(false);
    };

    const validateDetectionConfiguration = (): boolean => {
        let validationResult;
        if (
            (validationResult = validateJSON(newAlertJSON)) &&
            !validationResult.valid
        ) {
            // Validation failed
            setDetectionConfigurationError(true);
            setDetectionConfigurationHelperText(validationResult.message || "");

            return false;
        }

        setDetectionConfigurationError(false);
        setDetectionConfigurationHelperText("");
        setNewAlert(JSON.parse(newAlertJSON));

        return true;
    };

    const onSubWizardFinish = (subscriptionGroup: SubscriptionGroup): void => {
        props
            .onSubscriptionGroupWizardFinish(subscriptionGroup)
            // todo null check
            .then((newSub: SubscriptionGroup): void => {
                setSubs((s) => {
                    const a = cloneDeep(s);
                    a.push(newSub);

                    return a;
                });
                setLoading(false);
                setWizard("");
            });
    };

    const onSubWizardC = (): void => {
        setLoading(false);
        setWizard("");
    };

    const onSubscriptionGroupWizardStepChange = (
        step: SubscriptionGroupWizardStep
    ): void => {
        step;
    };

    const onReset = (): void => {
        const alert = editableAlert || createDefaultAlert();
        setNewAlert(alert);
        setNewAlertJSON(JSON.stringify(alert));
    };

    const stepLabelFn = (step: string): string => {
        return t(`label.${kebabCase(AlertWizardStep[+step])}`);
    };

    return (
        <>
            {wizard !== "sub" && (
                <>
                    {/* Stepper */}
                    <Grid container>
                        <Grid item sm={12}>
                            <StepperV1
                                activeStep={currentWizardStep.toString()}
                                stepLabelFn={stepLabelFn}
                                steps={Object.values(AlertWizardStep).reduce(
                                    (steps, alertWizardStep) => {
                                        if (
                                            typeof alertWizardStep === "number"
                                        ) {
                                            steps.push(
                                                alertWizardStep.toString()
                                            );
                                        }

                                        return steps;
                                    },
                                    [] as string[]
                                )}
                            />
                        </Grid>
                    </Grid>

                    <PageContentsCardV1>
                        <Grid container>
                            {/* Step label */}
                            <Grid item sm={12}>
                                <Typography variant="h5">
                                    {t(
                                        `label.${kebabCase(
                                            AlertWizardStep[currentWizardStep]
                                        )}`
                                    )}
                                </Typography>
                            </Grid>

                            {/* Spacer */}
                            <Grid item sm={12} />

                            {/* Detection configuration */}
                            {currentWizardStep ===
                                AlertWizardStep.DETECTION_CONFIGURATION && (
                                <>
                                    {/* Detection configuration editor */}
                                    <Grid item sm={12}>
                                        <JSONEditorV1<Alert>
                                            error={detectionConfigurationError}
                                            helperText={
                                                detectionConfigurationHelperText
                                            }
                                            value={newAlert}
                                            onChange={
                                                onDetectionConfigurationChange
                                            }
                                        />
                                    </Grid>

                                    {/* Alert evaluation */}
                                    <Grid item sm={12}>
                                        <AlertEvaluationTimeSeriesCard
                                            alertEvaluation={alertEvaluation}
                                            alertEvaluationTimeSeriesHeight={
                                                500
                                            }
                                            title="Preview Alert"
                                            onRefresh={refreshAlertEvaluation}
                                        />
                                    </Grid>
                                </>
                            )}

                            {/* Subscription groups */}
                            {!loading &&
                                currentWizardStep ===
                                    AlertWizardStep.SUBSCRIPTION_GROUPS && (
                                    <>
                                        {/* Detection configuration editor */}
                                        <Grid item sm={12}>
                                            <TransferList<SubscriptionGroup>
                                                fromLabel="All Subscription Groups"
                                                fromList={subs}
                                                listItemKeyFn={(
                                                    s: SubscriptionGroup
                                                ): number => s.id}
                                                listItemTextFn={(
                                                    s: SubscriptionGroup
                                                ): string => s.name}
                                                toLabel="Associated Subscription Groups"
                                                toList={selecteddSubs}
                                                onChange={setSelectedSubs}
                                            />
                                        </Grid>
                                    </>
                                )}

                            {/* Review and submit */}
                            {currentWizardStep ===
                                AlertWizardStep.REVIEW_AND_SUBMIT && (
                                <>
                                    {/* Alert information */}
                                    <Grid item sm={12}>
                                        <JSONEditorV1<Alert>
                                            readOnly
                                            value={newAlert}
                                        />
                                    </Grid>

                                    <Grid container item justify="flex-end">
                                        {/* Subscription groups */}
                                        <Grid item sm={2}>
                                            <Typography variant="subtitle1">
                                                <strong>
                                                    {t(
                                                        "label.subscription-groups"
                                                    )}
                                                </strong>
                                            </Typography>
                                        </Grid>

                                        {/* No subscription groups */}
                                        {isEmpty(selecteddSubs) && (
                                            <Grid item sm={10}>
                                                <Typography variant="body2">
                                                    {t("label.no-data-marker")}
                                                </Typography>
                                            </Grid>
                                        )}

                                        {/* All subscription groups */}
                                        {selecteddSubs && (
                                            <Grid item sm={10}>
                                                {selecteddSubs.map(
                                                    (sub, index) => (
                                                        <Typography
                                                            key={index}
                                                            variant="body2"
                                                        >
                                                            {sub.name}
                                                        </Typography>
                                                    )
                                                )}
                                            </Grid>
                                        )}
                                    </Grid>
                                </>
                            )}
                        </Grid>

                        {loading &&
                            currentWizardStep ===
                                AlertWizardStep.SUBSCRIPTION_GROUPS && (
                                <AppLoadingIndicatorV1 />
                            )}

                        {/* Spacer */}
                        <Box padding={2} />

                        {/* Controls */}
                        <Grid
                            container
                            alignItems="stretch"
                            className={alertWizardClasses.controlsContainer}
                            justify="flex-end"
                        >
                            {detectionConfigurationError && (
                                <Grid item sm={12}>
                                    <MuiAlert severity="error">
                                        There were some errors
                                    </MuiAlert>
                                </Grid>
                            )}

                            {/* Separator */}
                            <Grid item sm={12}>
                                <Box
                                    border={Dimension.WIDTH_BORDER_DEFAULT}
                                    borderBottom={0}
                                    borderColor={Palette.COLOR_BORDER_DEFAULT}
                                    borderLeft={0}
                                    borderRight={0}
                                />
                            </Grid>

                            <Grid item sm={12}>
                                <Grid container justify="space-between">
                                    {/* Cancel button */}
                                    <Grid item>
                                        <Grid container>
                                            {props.showCancel && (
                                                <Grid item>
                                                    <Button
                                                        color="primary"
                                                        size="large"
                                                        variant="outlined"
                                                        onClick={onCancel}
                                                    >
                                                        {t("label.cancel")}
                                                    </Button>
                                                </Grid>
                                            )}

                                            {currentWizardStep ===
                                                AlertWizardStep.DETECTION_CONFIGURATION && (
                                                <Grid item>
                                                    <Button
                                                        color="primary"
                                                        size="large"
                                                        variant="outlined"
                                                        onClick={onReset}
                                                    >
                                                        Reset
                                                    </Button>
                                                </Grid>
                                            )}

                                            {currentWizardStep ===
                                                AlertWizardStep.SUBSCRIPTION_GROUPS && (
                                                <Grid item>
                                                    <Button
                                                        color="primary"
                                                        size="large"
                                                        variant="outlined"
                                                        onClick={onCreateNew}
                                                    >
                                                        Create New Susbscription
                                                        Group
                                                    </Button>
                                                </Grid>
                                            )}
                                        </Grid>
                                    </Grid>

                                    <Grid item>
                                        <Grid container>
                                            {/* Back button */}
                                            <Grid item>
                                                <Button
                                                    color="primary"
                                                    disabled={
                                                        currentWizardStep ===
                                                        AlertWizardStep.DETECTION_CONFIGURATION
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
                                                <Button
                                                    color="primary"
                                                    size="large"
                                                    variant="contained"
                                                    onClick={onNext}
                                                >
                                                    {currentWizardStep ===
                                                    AlertWizardStep.REVIEW_AND_SUBMIT
                                                        ? t("label.finish")
                                                        : t("label.next")}
                                                </Button>
                                            </Grid>
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </PageContentsCardV1>
                </>
            )}

            {wizard === "sub" && (
                <SubscriptionGroupWizard
                    showCancel
                    alerts={alerts}
                    onCancel={onSubWizardC}
                    onChange={onSubscriptionGroupWizardStepChange}
                    onFinish={onSubWizardFinish}
                />
            )}
        </>
    );
};
