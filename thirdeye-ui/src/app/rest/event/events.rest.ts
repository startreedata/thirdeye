import axios from "axios";
import { EditableEvent, Event } from "../dto/event.interfaces";
import { GetAllEventsProps } from "./events.interfaces";

const BASE_URL_EVENT = "/api/events";

export const getEvent = async (id: number): Promise<Event> => {
    const response = await axios.get(`${BASE_URL_EVENT}/${id}`);

    return response.data;
};

export const getAllEvents = async ({
    startTime,
    endTime,
    type,
}: GetAllEventsProps = {}): Promise<Event[]> => {
    const queryParams = new URLSearchParams();

    if (startTime) {
        queryParams.set("startTime", `${startTime}`);
    }

    if (endTime) {
        queryParams.set("endTime", `${endTime}`);
    }

    if (type) {
        queryParams.set("type", type);
    }

    const response = await axios.get(
        `${BASE_URL_EVENT}?${queryParams.toString()}`
    );

    return response.data;
};

export const createEvent = async (event: EditableEvent): Promise<Event> => {
    const response = await axios.post(BASE_URL_EVENT, [event]);

    return response.data[0];
};

export const createEvents = async (events: Event[]): Promise<Event[]> => {
    const response = await axios.post(BASE_URL_EVENT, events);

    return response.data;
};

export const updateEvent = async (event: Event): Promise<Event> => {
    const response = await axios.put(BASE_URL_EVENT, [event]);

    return response.data[0];
};

export const updateEvents = async (events: Event[]): Promise<Event[]> => {
    const response = await axios.put(BASE_URL_EVENT, events);

    return response.data;
};

export const deleteEvent = async (id: number): Promise<Event> => {
    const response = await axios.delete(`${BASE_URL_EVENT}/${id}`);

    return response.data;
};
