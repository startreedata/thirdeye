import { ReactNode } from "react";

export type StepperProps = {
    steps: {
        label: string;
        content: StepContentType;
    }[];
    clickable?: boolean;
    currentStep?: number;
    onStepChange?: (step: number) => void;
};

export type StepContentType =
    | ReactNode
    | (({ onNext }: NextHandlerProps) => ReactNode);

export type NextHandlerProps = {
    onNext: () => void;
};
