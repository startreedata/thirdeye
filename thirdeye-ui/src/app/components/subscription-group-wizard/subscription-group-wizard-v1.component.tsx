import { Box, Button, Grid, Typography } from "@material-ui/core";
import { DimensionV1, PaletteV1 } from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect, useState } from "react";
import { Step, Steps, Wizard } from "react-albus";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { UiSubscriptionGroupAlert } from "../../rest/dto/ui-subscription-group.interfaces";
import { AppRoute } from "../../utils/routes/routes.util";
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
import { WizardStepper } from "../wizard-stepper/wizard-stepper.component";
import { SubscriptionGroupPropertiesForm } from "./subscription-group-properties-form/subscription-group-properties-form.component";
import { SubscriptionGroupRenderer } from "./subscription-group-renderer/subscription-group-renderer.component";
import { SubscriptionGroupWizardProps } from "./subscription-group-wizard.interfaces";

const subscriptionGroupWizardSteps = [
    "subscription-group-properties",
    "review-and-submit",
];

const FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES =
    "FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES";

export const SubscriptionGroupWizardV1: FunctionComponent<SubscriptionGroupWizardProps> = (
    props: SubscriptionGroupWizardProps
) => {
    const history = useHistory();
    const [
        newSubscriptionGroup,
        setNewSubscriptionGroup,
    ] = useState<SubscriptionGroup>(
        props.subscriptionGroup || createEmptySubscriptionGroup()
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

    const onSubmitSubscriptionGroupPropertiesForm = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        // Update subscription group with form inputs
        setNewSubscriptionGroup((newSubscriptionGroup) =>
            Object.assign(newSubscriptionGroup, subscriptionGroup)
        );
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

    const onFinish = (): void => {
        // On last step
        props.onFinish && props.onFinish(newSubscriptionGroup);
    };

    return (
        <Wizard
            basename={AppRoute.SUBSCRIPTION_GROUPS_CREATE}
            history={history}
            render={({ step, steps, previous, next }) => (
                <Grid container>
                    {/* Stepper */}
                    <Grid item sm={12}>
                        <WizardStepper />
                    </Grid>

                    {/* Steps */}
                    <Steps>
                        {/* Step 1 - Subscription group properties form */}
                        <Step id={subscriptionGroupWizardSteps[0]}>
                            {/* Subscription group properties form */}
                            <Grid item sm={12}>
                                <SubscriptionGroupPropertiesForm
                                    id={FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES}
                                    subscriptionGroup={newSubscriptionGroup}
                                    onSubmit={(subscriptionGroup) => {
                                        onSubmitSubscriptionGroupPropertiesForm(
                                            subscriptionGroup
                                        );
                                        next();
                                    }}
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
                        </Step>

                        {/* Step 2 - Review and submit */}
                        <Step id={subscriptionGroupWizardSteps[1]}>
                            {/* Subscription group information */}
                            <SubscriptionGroupRenderer
                                subscriptionGroup={newSubscriptionGroup}
                            />
                        </Step>
                    </Steps>

                    {/* Spacer */}
                    <Box padding={2} />

                    {/* Controls */}
                    <Grid
                        container
                        alignItems="stretch"
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
                        <Grid item>
                            <Grid container>
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
                                    {/* Submit button for subscription group properties form in
                                    first step */}
                                    {step.id !==
                                    subscriptionGroupWizardSteps[
                                        subscriptionGroupWizardSteps.length - 1
                                    ] ? (
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
                                    ) : (
                                        <Button
                                            color="primary"
                                            size="large"
                                            variant="contained"
                                            onClick={onFinish}
                                        >
                                            {t("label.finish")}
                                        </Button>
                                    )}
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            )}
        />
    );
};
