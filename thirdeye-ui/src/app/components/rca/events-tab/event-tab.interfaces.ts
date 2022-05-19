import { Event } from "../../../rest/dto/event.interfaces";

export interface EventsTabProps {
    onCheckClick: (events: Event[]) => void;
    selectedEvents: Event[];
}
