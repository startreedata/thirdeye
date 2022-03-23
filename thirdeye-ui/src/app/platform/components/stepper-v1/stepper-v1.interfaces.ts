// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export interface StepperV1Props {
    steps: string[];
    activeStep: string;
    stepLabelFn: (step: string) => string;
    disabled?: boolean;
    className?: string;
}
