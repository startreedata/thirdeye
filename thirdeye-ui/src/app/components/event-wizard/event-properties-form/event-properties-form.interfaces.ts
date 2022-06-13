import { EditableEvent, Event } from "../../../rest/dto/event.interfaces";

export interface EventPropertiesFormProps {
    id: string;
    event: EditableEvent;
    onSubmit?: (event: Event) => void;
}
