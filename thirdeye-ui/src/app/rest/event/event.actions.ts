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


import { useHTTPAction } from "../create-rest-action";
import { Event } from "../dto/event.interfaces";
import {
    GetAllEventsProps,
    GetEvent,
    GetEvents,
    GetEventsForAnomaly,
    GetEventsForAnomalyRestProps,
} from "./events.interfaces";
import {
    getAllEvents as getAllEventsRest,
    getEvent as getEventRest,
    getEventsForAnomaly as getEventsForAnomalyRest,
} from "./events.rest";

export const useGetEvent = (): GetEvent => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Event>(getEventRest);

    const getEvent = (id: number): Promise<Event | undefined> => {
        return makeRequest(id);
    };

    return { event: data, getEvent, status, errorMessages };
};

export const useGetEvents = (): GetEvents => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Event[]>(getAllEventsRest);

    const getEvents = (
        getEventsParams: GetAllEventsProps = {}
    ): Promise<Event[] | undefined> => {
        return makeRequest(getEventsParams);
    };

    return {
        events: data,
        getEvents,
        status,
        errorMessages,
    };
};

export const useGetEventsForAnomaly = (): GetEventsForAnomaly => {
    const { data, makeRequest, status, errorMessages } = useHTTPAction<Event[]>(
        getEventsForAnomalyRest
    );

    const getEventsForAnomaly = (
        getEventsParams: GetEventsForAnomalyRestProps
    ): Promise<Event[] | undefined> => {
        return makeRequest(getEventsParams);
    };

    return {
        events: data,
        getEventsForAnomaly,
        status,
        errorMessages,
    };
};
