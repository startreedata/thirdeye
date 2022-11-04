import { ReactElement } from "react";

export interface InputSectionProps {
    label?: string;
    helperLabel?: string;
    labelComponent?: ReactElement;
    inputComponent: ReactElement;
    fullWidth?: boolean;
}
