import { EditableAlert } from "../../../../rest/dto/alert.interfaces";

export interface AlertFrequencyProps<NewOrExistingAlert> {
    alert: NewOrExistingAlert;
    onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
}
