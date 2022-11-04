// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
