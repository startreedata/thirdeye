import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface AlertTemplateProps {
    alert: EditableAlert;
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
    selectedAlertTemplate: AlertTemplate | null;
    setSelectedAlertTemplate: (newAlertTemplate: AlertTemplate | null) => void;
    alertTemplateOptions: AlertTemplate[];
}
