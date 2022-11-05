import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface AlertDetailsProps {
    alert: EditableAlert;
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
}
