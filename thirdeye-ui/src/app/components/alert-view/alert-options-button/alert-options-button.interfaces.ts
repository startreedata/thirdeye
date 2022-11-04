import { MouseEvent, ReactNode } from "react";
import { Alert } from "../../../rest/dto/alert.interfaces";

export interface AlertOptionsButtonProps {
    alert: Alert;
    onChange?: (alert: Alert) => void;
    onDelete?: (alert: Alert) => void;
    onReset?: (alert: Alert) => void;
    showViewDetails?: boolean;
    openButtonRenderer?: (
        clickCallback: (event: MouseEvent<HTMLElement>) => void
    ) => ReactNode;
}
