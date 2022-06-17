import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface AlertDetailsProps<NewOrExistingAlert> {
    alert: NewOrExistingAlert;
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
}
