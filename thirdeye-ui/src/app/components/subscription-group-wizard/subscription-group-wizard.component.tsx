import { Box, Button, Grid, Typography } from "@material-ui/core";
import { PageContentsCardV1, StepperV1 } from "@startree-ui/platform-ui";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { UiSubscriptionGroupAlert } from "../../rest/dto/ui-subscription-group.interfaces";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import {
    createEmptySubscriptionGroup,
    getUiSubscriptionGroup,
    getUiSubscriptionGroupAlertId,
    getUiSubscriptionGroupAlertName,
    getUiSubscriptionGroupAlerts,
} from "../../utils/subscription-groups/subscription-groups.util";
import { validateEmail } from "../../utils/validation/validation.util";
import { EditableList } from "../editable-list/editable-list.component";
import { TransferList } from "../transfer-list/transfer-list.component";
import { SubscriptionGroupPropertiesForm } from "./subscription-group-properties-form/subscription-group-properties-form.component";
import { SubscriptionGroupRenderer } from "./subscription-group-renderer/subscription-group-renderer.component";
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
    const [
        newSubscriptionGroup,
        setNewSubscriptionGroup,
    ] = useState<SubscriptionGroup>(
        props.subscriptionGroup || createEmptySubscriptionGroup()
    );
    const [
        currentWizardStep,
        setCurrentWizardStep,
    ] = useState<SubscriptionGroupWizardStep>(
        SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
    );
    const { t } = useTranslation();

    useEffect(() => {
        // In case of input subscription group, alerts need to be configured for included alerts
        // don't carry name
        if (props.subscriptionGroup) {
            newSubscriptionGroup.alerts = getUiSubscriptionGroup(
                props.subscriptionGroup,
                props.alerts
            ).alerts as Alert[];
        }
    }, []);

    useEffect(() => {
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

    const onUiSubscriptionGroupAlertsChange = (
        uiSubscriptionGroupAlerts: UiSubscriptionGroupAlert[]
    ): void => {
        // Update subscription group with subscribed alerts
        setNewSubscriptionGroup(
            (newSubscriptionGroup): SubscriptionGroup => {
                newSubscriptionGroup.alerts = uiSubscriptionGroupAlerts as Alert[];

                return newSubscriptionGroup;
            }
        );
    };

    const onSubscriptionGroupEmailsChange = (emails: string[]): void => {
        // Update subscription group with subscribed emails
        setNewSubscriptionGroup(
            (newSubscriptionGroup): SubscriptionGroup => {
                if (newSubscriptionGroup.notificationSchemes.email) {
                    // Add to existing email settings
                    newSubscriptionGroup.notificationSchemes.email.to = emails;
                } else {
                    // Create and add to email settings
                    newSubscriptionGroup.notificationSchemes.email = {
                        to: emails,
                    } as EmailScheme;
                }

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

        // Determine previous step
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

        // Determine next step
        setCurrentWizardStep(
            SubscriptionGroupWizardStep[
                SubscriptionGroupWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof SubscriptionGroupWizardStep
            ]
        );
    };

    const stepLabelFn = (step: string): string => {
        return t(`label.${kebabCase(SubscriptionGroupWizardStep[+step])}`);
    };

    return (
        <>
            {/* Stepper */}
            <Grid container>
                <Grid item sm={12}>
                    <StepperV1
                        activeStep={currentWizardStep.toString()}
                        stepLabelFn={stepLabelFn}
                        steps={Object.values(
                            SubscriptionGroupWizardStep
                        ).reduce((steps, subscriptionGroupWizardStep) => {
                            if (
                                typeof subscriptionGroupWizardStep === "number"
                            ) {
                                steps.push(
                                    subscriptionGroupWizardStep.toString()
                                );
                            }

                            return steps;
                        }, [] as string[])}
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
                                    SubscriptionGroupWizardStep[
                                        currentWizardStep
                                    ]
                                )}`
                            )}
                        </Typography>
                    </Grid>

                    {/* Spacer */}
                    <Grid item sm={12} />

                    {/* Subscription group properties */}
                    {currentWizardStep ===
                        SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES && (
                        <>
                            {/* Subscription group properties form */}
                            <Grid item sm={12}>
                                <SubscriptionGroupPropertiesForm
                                    id={FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES}
                                    subscriptionGroup={newSubscriptionGroup}
                                    onSubmit={
                                        onSubmitSubscriptionGroupPropertiesForm
                                    }
                                />
                            </Grid>

                            {/* Spacer */}
                            <Grid item sm={12} />

                            {/* Subscribe alerts */}
                            <Grid item sm={12}>
                                <Typography variant="h5">
                                    {t("label.subscribe-alerts")}
                                </Typography>
                            </Grid>

                            <Grid item sm={12}>
                                <TransferList<UiSubscriptionGroupAlert>
                                    fromLabel={t("label.all-entity", {
                                        entity: t("label.alerts"),
                                    })}
                                    fromList={getUiSubscriptionGroupAlerts(
                                        props.alerts
                                    )}
                                    listItemKeyFn={
                                        getUiSubscriptionGroupAlertId
                                    }
                                    listItemTextFn={
                                        getUiSubscriptionGroupAlertName
                                    }
                                    toLabel={t("label.subscribed-alerts")}
                                    toList={
                                        getUiSubscriptionGroup(
                                            newSubscriptionGroup,
                                            props.alerts
                                        ).alerts
                                    }
                                    onChange={onUiSubscriptionGroupAlertsChange}
                                />
                            </Grid>

                            {/* Spacer */}
                            <Grid item sm={12} />

                            {/* Subscribe emails */}
                            <Grid item sm={12}>
                                <Typography variant="h5">
                                    {t("label.subscribe-emails")}
                                </Typography>
                            </Grid>

                            <Grid item sm={12}>
                                <EditableList
                                    addButtonLabel={t("label.add")}
                                    inputLabel={t("label.add-entity", {
                                        entity: t("label.email"),
                                    })}
                                    list={
                                        (newSubscriptionGroup &&
                                            newSubscriptionGroup
                                                .notificationSchemes.email &&
                                            newSubscriptionGroup
                                                .notificationSchemes.email
                                                .to) ||
                                        []
                                    }
                                    validateFn={validateEmail}
                                    onChange={onSubscriptionGroupEmailsChange}
                                />
                            </Grid>
                        </>
                    )}

                    {/* Review and submit */}
                    {currentWizardStep ===
                        SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT && (
                        <>
                            {/* Subscription group information */}
                            <SubscriptionGroupRenderer
                                subscriptionGroup={newSubscriptionGroup}
                            />
                        </>
                    )}
                </Grid>

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
                                        {/* Submit button for subscription group properties form in
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
            </PageContentsCardV1>
        </>
    );
};
