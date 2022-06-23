import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface AlertTemplateProps {
    alert: EditableAlert;
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
    selectedAlertTemplate: AlertTemplate;
    setSelectedAlertTemplate: (newAlertTemplate: AlertTemplate) => void;
    alertTemplateOptions: AlertTemplate[];
}
