import { Step, StepLabel, Stepper } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { WithWizard } from "react-albus";
import { useTranslation } from "react-i18next";

export const WizardStepper: FunctionComponent = () => {
    const { t } = useTranslation();

    return (
        <WithWizard
            render={({ step, steps }) => (
                <Stepper alternativeLabel activeStep={steps.indexOf(step)}>
                    {steps.map((step, index) => (
                        <Step key={index}>
                            <StepLabel>
                                {step.id && t(`label.${step.id}`)}
                            </StepLabel>
                        </Step>
                    ))}
                </Stepper>
            )}
        />
    );
};
