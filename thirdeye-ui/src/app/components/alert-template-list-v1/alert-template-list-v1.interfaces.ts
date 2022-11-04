import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";

export interface AlertTemplateListV1Props {
    alertTemplates: AlertTemplate[] | null;
    onChange?: (alertTemplate: AlertTemplate) => void;
    onDelete?: (alertTemplates: AlertTemplate[]) => void;
}
