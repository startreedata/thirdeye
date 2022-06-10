///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


import axios from "axios";
import { EditableEvent, Event } from "../dto/event.interfaces";
import {
    GetAllEventsProps,
    GetEventsForAnomalyRestProps,
} from "./events.interfaces";

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
        queryParams.set("startTime", `[gte]${startTime}`);
    }

    if (endTime) {
        queryParams.set("endTime", `[lte]${endTime}`);
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

export const getEventsForAnomaly = async ({
    anomalyId,
    eventType,
    limit,
    lookaround,
    scoring,
}: GetEventsForAnomalyRestProps): Promise<Event[]> => {
    const queryParams = new URLSearchParams([
        ["anomalyId", anomalyId.toString()],
    ]);

    if (eventType) {
        queryParams.set("eventType", eventType);
    }

    if (limit) {
        queryParams.set("limit", limit.toString());
    }

    if (lookaround) {
        queryParams.set("lookaround", lookaround);
    }

    if (scoring) {
        queryParams.set("scoring", scoring);
    }

    const response = await axios.get(
        `/api/rca/related/events?${queryParams.toString()}`
    );

    return response.data;
};
