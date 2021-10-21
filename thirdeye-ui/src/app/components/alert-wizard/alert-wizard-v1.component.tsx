import { Box, Button, Grid, Step, Typography } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
    DimensionV1,
    PaletteV1,
} from "@startree-ui/platform-ui";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { Steps, Wizard, WizardContext } from "react-albus";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createDefaultAlert,
    omitNonUpdatableData,
} from "../../utils/alerts/alerts.util";
import { AppRoute } from "../../utils/routes/routes.util";
import { validateJSON } from "../../utils/validation/validation.util";
import { JSONEditor } from "../json-editor/json-editor.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TransferList } from "../transfer-list/transfer-list.component";
import { AlertEvaluationTimeSeriesCard } from "../visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { WizardStepper } from "../wizard-stepper/wizard-stepper.component";
import { AlertWizardProps } from "./alert-wizard.interfaces";
import { useAlertWizardStyles } from "./alert-wizard.styles";

const alertWizardSteps = [
    "datasource-configuration",
    "subscription-groups",
    "review-and-submit",
];

export const AlertWizardV1: FunctionComponent<AlertWizardProps> = (
    props: AlertWizardProps
) => {
    const alertWizardClasses = useAlertWizardStyles();
    const [subLoading, setSubLoading] = useState(true);
    const history = useHistory();
    const editableAlert = props.alert
        ? omitNonUpdatableData(props.alert)
        : undefined;
    const [newAlert, setNewAlert] = useState<Alert>(
        editableAlert || createDefaultAlert()
    );
    const [newAlertJSON, setNewAlertJSON] = useState(
        JSON.stringify(editableAlert || createDefaultAlert())
    );
    const [
        detectionConfigurationError,
        setDetectionConfigurationError,
    ] = useState(false);
    const [
        detectionConfigurationHelperText,
        setDetectionConfigurationHelperText,
    ] = useState("");
    const [subs, setSubs] = useState<SubscriptionGroup[]>([]);

    const [selecteddSubs, setSelectedSubs] = useState<SubscriptionGroup[]>([]);
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);

    const { timeRangeDuration } = useTimeRange();
    const { t } = useTranslation();

    useEffect(() => {
        refreshAlertEvaluation();
    }, [timeRangeDuration]);

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

    const onFinish = (): void => {
        props.onFinish && props.onFinish(newAlert, selecteddSubs);
    };

    const handleNext = (wizardContext: WizardContext): void => {
        if (wizardContext.step.id === alertWizardSteps[0]) {
            props.getAllSubscriptionGroups &&
                props
                    .getAllSubscriptionGroups()
                    .then((subs: SubscriptionGroup[]): void => {
                        setSubs(subs);
                    })
                    .finally(() => setSubLoading(false));
        }
        wizardContext.push();
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

    return (
        <Wizard
            basename={AppRoute.ALERTS_CREATE}
            history={history}
            render={({ step, steps, previous, next }) => (
                <>
                    <Grid container>
                        {/* Stepper */}
                        <Grid item sm={12}>
                            <WizardStepper />
                        </Grid>

                        {/* Step label */}
                        <Grid item sm={12}>
                            <Typography variant="h5">
                                {step.id && t(`label.${step.id}`)}
                            </Typography>
                        </Grid>

                        {/* Spacer */}
                        <Grid item sm={12} />

                        {/* Alert wizard steps */}
                        <Grid item sm={12}>
                            <Steps>
                                {/* Step 1 */}
                                <Step id={alertWizardSteps[0]}>
                                    {/* Detection configuration editor */}
                                    <Grid item sm={12}>
                                        <JSONEditor
                                            error={detectionConfigurationError}
                                            helperText={
                                                detectionConfigurationHelperText
                                            }
                                            value={
                                                (newAlert as unknown) as Record<
                                                    string,
                                                    unknown
                                                >
                                            }
                                            onChange={
                                                onDetectionConfigurationChange
                                            }
                                        />
                                    </Grid>

                                    {/* Spacer */}
                                    <Box padding={2} />

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
                                </Step>

                                {/* Step 2 */}
                                <Step id={alertWizardSteps[1]}>
                                    {/* Subscription groups List */}
                                    <Grid item sm={12}>
                                        {subLoading ? (
                                            <AppLoadingIndicatorV1 />
                                        ) : (
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
                                        )}
                                    </Grid>
                                </Step>

                                {/* Step 3 */}
                                <Step id={alertWizardSteps[2]}>
                                    {/* Alert information */}
                                    <Grid item sm={12}>
                                        <JSONEditor
                                            readOnly
                                            value={
                                                (newAlert as unknown) as Record<
                                                    string,
                                                    unknown
                                                >
                                            }
                                        />
                                    </Grid>

                                    <Grid container justify="flex-end">
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
                                                <Typography variant="body1">
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
                                                            variant="body1"
                                                        >
                                                            {sub.name}
                                                        </Typography>
                                                    )
                                                )}
                                            </Grid>
                                        )}
                                    </Grid>
                                </Step>
                            </Steps>
                        </Grid>
                    </Grid>

                    {/* Spacer */}
                    <Box padding={2} />

                    {/* Controls */}
                    <Grid
                        container
                        alignItems="stretch"
                        className={alertWizardClasses.controlsContainer}
                        justifyContent="flex-end"
                    >
                        {/* Separator Line */}
                        <Grid item sm={12}>
                            <Box
                                border={DimensionV1.BorderWidthDefault}
                                borderBottom={0}
                                borderColor={PaletteV1.BorderColorDefault}
                                borderLeft={0}
                                borderRight={0}
                            />
                        </Grid>
                        <Grid item sm={12}>
                            <Grid container justifyContent="flex-end">
                                {/* Back button */}
                                <Grid item>
                                    <Button
                                        color="primary"
                                        disabled={steps.indexOf(step) < 1}
                                        size="large"
                                        variant="outlined"
                                        onClick={previous}
                                    >
                                        {t("label.back")}
                                    </Button>
                                </Grid>

                                <Grid item>
                                    {/* Next/Finish button */}
                                    <Button
                                        color="primary"
                                        size="large"
                                        variant="contained"
                                        onClick={
                                            steps.indexOf(step) ===
                                            steps.length - 1
                                                ? onFinish
                                                : next
                                        }
                                    >
                                        {steps.indexOf(step) ===
                                        steps.length - 1
                                            ? t("label.finish")
                                            : t("label.next")}
                                    </Button>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </>
            )}
            onNext={handleNext}
        />
    );
};
