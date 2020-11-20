import { ReactNode } from "react";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";

export interface ConfigStepsProps {
    name: string;
    extraFields: ReactNode;
    showPreviewButton?: boolean;
    config: string;
    previewData?: AlertEvaluation;
    onConfigChange: (newValue: string) => void;
    onResetConfig: () => void;
    onPreviewAlert?: () => void;
}
