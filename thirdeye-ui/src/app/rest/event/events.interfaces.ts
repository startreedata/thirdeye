import { ActionHook } from "../actions.interfaces";
import { Event } from "../dto/event.interfaces";

export interface GetAllEventsProps {
    startTime?: number;
    endTime?: number;
    type?: string;
}

export interface GetEvent extends ActionHook {
    event: Event | null;
    getEvent: (eventId: number) => Promise<Event | undefined>;
}

export interface GetEvents extends ActionHook {
    events: Event[] | null;
    getEvents: (
        getEventsParams?: GetAllEventsProps
    ) => Promise<Event[] | undefined>;
}

export interface GetEventsForAnomalyRestProps {
    anomalyId: number;
    eventType?: string;
    limit?: number;
    lookaround?: string;
    scoring?: string;
}

export interface GetEventsForAnomaly extends ActionHook {
    events: Event[] | null;
    getEventsForAnomaly: (
        getEventsForAnomalyParams: GetEventsForAnomalyRestProps
    ) => Promise<Event[] | undefined>;
}
