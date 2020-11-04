import { Step, StepButton, StepLabel, Stepper } from "@material-ui/core";
import React, { FunctionComponent, ReactNode, useState } from "react";

type Props = {
    steps: {
        label: string;
        content: ReactNode;
    }[];
    clickable?: boolean;
};

export const CustomStepper: FunctionComponent<Props> = ({
    steps,
    clickable,
}: Props) => {
    const [activeStep, setActiveStep] = useState(0);
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
    };

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
            {activeStep > -1 && activeStep < steps.length
                ? steps[activeStep].content
                : null}
        </>
    );
};
