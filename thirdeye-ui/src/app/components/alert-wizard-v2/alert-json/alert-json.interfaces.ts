import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface AlertJsonProps {
    alert: EditableAlert;
    onAlertPropertyChange: (
        contents: Partial<EditableAlert>,
        fullReplace: boolean
    ) => void;
}
