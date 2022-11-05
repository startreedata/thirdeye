export interface StepperV1Props {
    steps: string[];
    activeStep: string;
    stepLabelFn: (step: string) => string;
    disabled?: boolean;
    className?: string;
}
