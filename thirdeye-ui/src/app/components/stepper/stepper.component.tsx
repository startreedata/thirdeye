import { Step, StepButton, StepLabel, Stepper } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { StepperProps } from "./stepper.interfaces";

export const CustomStepper: FunctionComponent<StepperProps> = ({
    steps,
    clickable,
    currentStep: controlledStep,
    onStepChange,
}: StepperProps) => {
    const [activeStep, setActiveStep] = useState(controlledStep || 0);
    // Will be implemented
    const [completed] = useState(new Set());
    const [skipped] = useState(new Set());

    const isStepSkipped = (step: number): boolean => {
        return skipped.has(step);
    };

    const isStepComplete = (step: number): boolean => {
        return completed.has(step);
    };

    const handleStep = (step: number) => (): void => {
        setActiveStep(step);
        onStepChange && onStepChange(step);
    };

    const handleNext = (): void => {
        setActiveStep(activeStep + 1);
        onStepChange && onStepChange(activeStep + 1);
    };

    useEffect(() => {
        setActiveStep(controlledStep || 0);
    }, [controlledStep]);

    const currentStep = steps[activeStep].content;

    return (
        <>
            <Stepper alternativeLabel nonLinear activeStep={activeStep}>
                {steps.map((step, index) => {
                    const stepProps: { completed?: boolean } = {};
                    const buttonProps = {};
                    if (isStepSkipped(index)) {
                        stepProps.completed = false;
                    }

                    return (
                        <Step key={step.label} {...stepProps}>
                            {clickable ? (
                                <StepButton
                                    completed={isStepComplete(index)}
                                    onClick={handleStep(index)}
                                    {...buttonProps}
                                >
                                    {step.label}
                                </StepButton>
                            ) : (
                                <StepLabel>{step.label}</StepLabel>
                            )}
                        </Step>
                    );
                })}
            </Stepper>
            {currentStep
                ? typeof currentStep === "function"
                    ? currentStep({ onNext: handleNext })
                    : currentStep
                : null}
        </>
    );
};
