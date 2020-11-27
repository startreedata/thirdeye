import { ReactNode } from "react";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { CommonCodeMirrorProps } from "../editor/code-mirror.interfaces";

export interface ConfigStepsProps {
    name: string;
    extraFields: ReactNode;
    showPreviewButton?: boolean;
    config: string;
    previewData?: AlertEvaluation;
    onConfigChange: (newValue: string) => void;
    onResetConfig: () => void;
    onPreviewAlert?: () => void;
    editorProps?: CommonCodeMirrorProps;
}
