import { Event } from "../../../rest/dto/event.interfaces";

export interface EventPropertiesFormProps {
    id: string;
    event: Event;
    onSubmit?: (event: Event) => void;
}
