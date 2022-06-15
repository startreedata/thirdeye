import { Event } from "../../../rest/dto/event.interfaces";

export interface EventsTabProps {
    anomalyId: number;
    onCheckClick: (events: Event[]) => void;
    selectedEvents: Event[];
    searchValue: string;
}
