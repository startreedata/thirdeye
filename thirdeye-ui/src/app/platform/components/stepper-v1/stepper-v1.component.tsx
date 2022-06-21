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
