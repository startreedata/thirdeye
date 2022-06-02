import { Event } from "../../rest/dto/event.interfaces";

export interface EventListV1Props {
    events: Event[] | null;
    onDelete?: (event: Event) => void;
}
