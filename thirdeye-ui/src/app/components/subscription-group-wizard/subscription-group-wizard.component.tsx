/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Accordion,
    AccordionSummary,
    Box,
    Button,
    Grid,
    Typography,
} from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
    StepperV1,
} from "../../platform/components";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    NotificationSpec,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import {
    createEmptySubscriptionGroup,
    getUiSubscriptionGroup,
} from "../../utils/subscription-groups/subscription-groups.util";
import { GroupsEditor } from "./groups-editor/groups-editor.component";
import { SubscriptionGroupPropertiesForm } from "./subscription-group-properties-form/subscription-group-properties-form.component";
import { SubscriptionGroupRenderer } from "./subscription-group-renderer/subscription-group-renderer.component";
import {
    SubscriptionGroupWizardProps,
    SubscriptionGroupWizardStep,
} from "./subscription-group-wizard.interfaces";
import { useSubscriptionGroupWizardStyles } from "./subscription-group-wizard.styles";

const FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES =
    "FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES";

function generateDefaultSelection(
    subscriptionGroup: SubscriptionGroup | undefined | null,
    alerts: Alert[]
): DataGridSelectionModelV1<Alert> {
    if (subscriptionGroup && alerts) {
        const selectedIds = subscriptionGroup.alerts.map(
            (alert: { id: number }) => alert.id
        );
        const selectedAlerts = alerts.filter((alert: Alert) =>
            selectedIds.includes(alert.id)
        );

        return {
            rowKeyValues: selectedIds,
            rowKeyValueMap: new Map(
                selectedAlerts.map((alert: Alert) => [alert.id, alert])
            ),
        };
    }

    return {
        rowKeyValues: [],
        rowKeyValueMap: new Map(),
    };
}

export const SubscriptionGroupWizard: FunctionComponent<
    SubscriptionGroupWizardProps
> = (props: SubscriptionGroupWizardProps) => {
    const subscriptionGroupWizardClasses = useSubscriptionGroupWizardStyles();
    const [newSubscriptionGroup, setNewSubscriptionGroup] =
        useState<SubscriptionGroup>(
            props.subscriptionGroup || createEmptySubscriptionGroup()
        );
    const [currentWizardStep, setCurrentWizardStep] =
        useState<SubscriptionGroupWizardStep>(
            SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES
        );
    const [selectedAlertsGroup, setSelectedAlertsGroup] = useState<
        DataGridSelectionModelV1<Alert>
    >(generateDefaultSelection(props.subscriptionGroup, props.alerts));
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

    const onSubscriptionGroupEmailsChange = (emails: string[]): void => {
        // Update subscription group with subscribed emails
        setNewSubscriptionGroup((newSubscriptionGroup): SubscriptionGroup => {
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
        });
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

    const handleSpecsChange = (specs: NotificationSpec[]): void => {
        newSubscriptionGroup.specs = specs;
    };

    const alertsColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 150,
            flex: 1,
        },
    ];

    const handleSelectedAlertsChange = (
        updated: DataGridSelectionModelV1<Alert>
    ): void => {
        if (props.alerts) {
            let newlySelectedAlertsList: Alert[] = [];

            if (updated && updated.rowKeyValueMap) {
                const selectedById = updated.rowKeyValueMap;

                newlySelectedAlertsList = props.alerts.filter((group) =>
                    selectedById.has(group.id)
                );
            }

            // Update subscription group with subscribed alerts
            setNewSubscriptionGroup(
                (newSubscriptionGroup): SubscriptionGroup => {
                    newSubscriptionGroup.alerts = newlySelectedAlertsList;

                    return newSubscriptionGroup;
                }
            );
        }
        setSelectedAlertsGroup(updated);
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

            {/* Subscription group properties */}
            {currentWizardStep ===
                SubscriptionGroupWizardStep.SUBSCRIPTION_GROUP_PROPERTIES && (
                <>
                    <Grid container>
                        <Grid item xs={12}>
                            <PageContentsCardV1>
                                <Typography variant="h5">
                                    {t(
                                        `label.${kebabCase(
                                            SubscriptionGroupWizardStep[
                                                currentWizardStep
                                            ]
                                        )}`
                                    )}
                                </Typography>
                                <Box marginTop={3}>
                                    <SubscriptionGroupPropertiesForm
                                        id={
                                            FORM_ID_SUBSCRIPTION_GROUP_PROPERTIES
                                        }
                                        subscriptionGroup={newSubscriptionGroup}
                                        onSubmit={
                                            onSubmitSubscriptionGroupPropertiesForm
                                        }
                                    />
                                </Box>
                            </PageContentsCardV1>
                        </Grid>

                        <Grid item xs={12}>
                            <Accordion
                                defaultExpanded={false}
                                variant="outlined"
                            >
                                {/* Header */}
                                <AccordionSummary
                                    expandIcon={<ExpandMoreIcon />}
                                >
                                    <div>
                                        <div>
                                            <Typography variant="h5">
                                                {t("label.subscribe-alerts")}
                                            </Typography>
                                        </div>
                                        <div>
                                            <Typography variant="caption">
                                                {newSubscriptionGroup.alerts
                                                    .length > 0 &&
                                                    t(
                                                        "message.num-alerts-subscribed",
                                                        {
                                                            num: newSubscriptionGroup
                                                                .alerts.length,
                                                        }
                                                    )}
                                                {newSubscriptionGroup.alerts
                                                    .length === 0 &&
                                                    t(
                                                        "message.no-alerts-subscribed"
                                                    )}
                                            </Typography>
                                        </div>
                                    </div>
                                </AccordionSummary>
                                <Grid container>
                                    <Grid item lg={6} md={8} sm={12} xs={12}>
                                        <Box height={500}>
                                            <DataGridV1<Alert>
                                                hideBorder
                                                // hideToolbar
                                                columns={alertsColumns}
                                                data={props.alerts}
                                                rowKey="id"
                                                selectionModel={
                                                    selectedAlertsGroup
                                                }
                                                onSelectionChange={
                                                    handleSelectedAlertsChange
                                                }
                                            />
                                        </Box>
                                    </Grid>
                                </Grid>
                            </Accordion>
                        </Grid>

                        <Grid item xs={12}>
                            <PageContentsCardV1>
                                <Typography variant="h5">
                                    {t("label.channels")}
                                </Typography>
                                <Box marginTop={3}>
                                    <GroupsEditor
                                        subscriptionGroup={newSubscriptionGroup}
                                        onSpecsChange={handleSpecsChange}
                                        onSubscriptionGroupEmailsChange={
                                            onSubscriptionGroupEmailsChange
                                        }
                                    />
                                </Box>
                            </PageContentsCardV1>
                        </Grid>
                    </Grid>
                </>
            )}

            {/* Review and submit */}
            {currentWizardStep ===
                SubscriptionGroupWizardStep.REVIEW_AND_SUBMIT && (
                <Grid container>
                    <Grid item sm={12}>
                        <PageContentsCardV1>
                            {/* Subscription group information */}
                            <SubscriptionGroupRenderer
                                subscriptionGroup={newSubscriptionGroup}
                            />
                        </PageContentsCardV1>
                    </Grid>
                </Grid>
            )}

            {/* Controls */}
            <Grid container>
                <Grid item sm={12}>
                    <PageContentsCardV1>
                        <Grid
                            container
                            alignItems="stretch"
                            className={
                                subscriptionGroupWizardClasses.controlsContainer
                            }
                            direction="column"
                            justifyContent="flex-end"
                        >
                            <Grid item>
                                <Grid container justifyContent="space-between">
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
                </Grid>
            </Grid>
        </>
    );
};
