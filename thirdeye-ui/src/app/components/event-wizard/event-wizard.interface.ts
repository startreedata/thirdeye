import { EditableEvent, Event } from "../../rest/dto/event.interfaces";

export interface EventWizardProps {
    event?: Event | EditableEvent;
    showCancel?: boolean;
    fullWidth?: boolean;
    onCancel?: () => void;
    onChange?: (event: EditableEvent) => void;
    onSubmit?: (event: EditableEvent) => void;
}
