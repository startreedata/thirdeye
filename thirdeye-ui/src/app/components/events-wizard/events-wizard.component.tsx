import { Box, Button, Grid, Typography } from "@material-ui/core";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1, StepperV1 } from "../../platform/components";
import { Event } from "../../rest/dto/event.interfaces";
import { createEmptyEvent, getUiEvent } from "../../utils/events/events.util";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { EventsWizardStep, EventWizardProps } from "./event-wizard.interface";
import { EventPropertiesForm } from "./events-properties-form/event-properties-form.component";
import { EventRenderer } from "./events-renderer/events-renderer.component";
import { useEventWizardStyles } from "./events-wizard.styles";

const FORM_ID_EVENT_PROPERTIES = "FORM_ID_EVENT_PROPERTIES";

export const EventsWizard: FunctionComponent<EventWizardProps> = (
    props: EventWizardProps
) => {
    const [newEvent, setNewEvent] = useState<Event>(
        props.event || createEmptyEvent()
    );
    const [currentWizardStep, setCurrentWizardStep] =
        useState<EventsWizardStep>(EventsWizardStep.EVENT_PROPERTIES);
    const { t } = useTranslation();

    const eventWizardStyles = useEventWizardStyles();

    useEffect(() => {
        // Notify
        props.onChange && props.onChange(currentWizardStep);
    }, [currentWizardStep]);

    const onNext = (): void => {
        if (currentWizardStep === EventsWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            props.onFinish && props.onFinish(newEvent);

            return;
        }

        // Determine next step
        setCurrentWizardStep(
            EventsWizardStep[
                EventsWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof EventsWizardStep
            ]
        );
    };

    const onSubmitEventsPropertiesForm = (event: Event): void => {
        // Update event with form inputs
        setNewEvent((newEvent) => Object.assign(newEvent, event));

        // Next step
        onNext();
    };

    const stepLabelFn = (step: string): string => {
        return t(`label.${kebabCase(EventsWizardStep[+step])}`);
    };

    const onBack = (): void => {
        if (currentWizardStep === EventsWizardStep.EVENT_PROPERTIES) {
            // Already on first step
            return;
        }

        // Determine previous step
        setCurrentWizardStep(
            EventsWizardStep[
                EventsWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof EventsWizardStep
            ]
        );
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    return (
        <>
            {/* Stepper */}
            <Grid container>
                <Grid item sm={12}>
                    <StepperV1
                        activeStep={currentWizardStep.toString()}
                        stepLabelFn={stepLabelFn}
                        steps={Object.values(EventsWizardStep).reduce(
                            (steps, eventsWizardStep) => {
                                if (typeof eventsWizardStep === "number") {
                                    steps.push(eventsWizardStep.toString());
                                }

                                return steps;
                            },
                            [] as string[]
                        )}
                    />
                </Grid>

                <PageContentsCardV1 className={eventWizardStyles.pageContents}>
                    <Grid container>
                        {/* Step label */}
                        <Grid item sm={12}>
                            <Typography variant="h5">
                                {t(
                                    `label.${kebabCase(
                                        EventsWizardStep[currentWizardStep]
                                    )}`
                                )}
                            </Typography>
                        </Grid>

                        {/* Spacer */}
                        <Grid item sm={12} />

                        {/* Event properties */}
                        {currentWizardStep ===
                            EventsWizardStep.EVENT_PROPERTIES && (
                            <>
                                {/* Event Properties Form */}
                                <Grid item xs={12}>
                                    <EventPropertiesForm
                                        event={newEvent}
                                        id={FORM_ID_EVENT_PROPERTIES}
                                        onSubmit={onSubmitEventsPropertiesForm}
                                    />
                                </Grid>
                            </>
                        )}

                        {/* Review and submit */}
                        {currentWizardStep ===
                            EventsWizardStep.REVIEW_AND_SUBMIT && (
                            <>
                                {/* Event information */}
                                <EventRenderer uiEvent={getUiEvent(newEvent)} />
                            </>
                        )}

                        {/* Spacer */}
                        <Box width="100%" />

                        {/* Controls */}
                        <Grid
                            container
                            alignItems="stretch"
                            className={eventWizardStyles.controlsContainer}
                            direction="column"
                            justifyContent="flex-end"
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
                                                        EventsWizardStep.EVENT_PROPERTIES
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
                                                {/* Submit button for event properties form in
                                    first step */}
                                                {currentWizardStep ===
                                                    EventsWizardStep.EVENT_PROPERTIES && (
                                                    <Button
                                                        color="primary"
                                                        form={
                                                            FORM_ID_EVENT_PROPERTIES
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
                                                    EventsWizardStep.EVENT_PROPERTIES && (
                                                    <Button
                                                        color="primary"
                                                        size="large"
                                                        variant="contained"
                                                        onClick={onNext}
                                                    >
                                                        {currentWizardStep ===
                                                        EventsWizardStep.REVIEW_AND_SUBMIT
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
                    </Grid>
                </PageContentsCardV1>
            </Grid>
        </>
    );
};
