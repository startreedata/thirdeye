import {
    Step,
    StepLabel,
    Stepper,
    useMediaQuery,
    useTheme,
} from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { StepperV1Props } from "./stepper-v1.interfaces";
import { useStepperV1Styles } from "./stepper-v1.styles";

export const StepperV1: FunctionComponent<StepperV1Props> = ({
    steps,
    activeStep,
    stepLabelFn,
    disabled,
    className,
    ...otherProps
}) => {
    const stepperV1Classes = useStepperV1Styles();
    const theme = useTheme();
    const smUp = useMediaQuery(theme.breakpoints.up("sm"));

    return (
        <>
            {steps && (
                <Stepper
                    {...otherProps}
                    alternativeLabel
                    activeStep={disabled ? -1 : steps.indexOf(activeStep)}
                    className={classNames(
                        stepperV1Classes.stepper,
                        className,
                        "stepper-v1"
                    )}
                >
                    {steps.map((eachStep, index) => (
                        <Step className="stepper-v1-step" key={index}>
                            <StepLabel className="stepper-v1-step-label">
                                {smUp && stepLabelFn(eachStep)}
                            </StepLabel>
                        </Step>
                    ))}
                </Stepper>
            )}
        </>
    );
};
