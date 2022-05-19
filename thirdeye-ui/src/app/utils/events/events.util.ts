import i18n from "i18next";
import { isEmpty } from "lodash";
import { formatDateAndTimeV1 } from "../../platform/utils";
import { Event } from "../../rest/dto/event.interfaces";
import { UiEvent } from "../../rest/dto/ui-event.interfaces";

export const createEmptyEvent = (): Event => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        startTime: 1,
        endTime: 1,
        type: noDataMarker,
    };
};

export const createEmptyUiEvent = (): UiEvent => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        startTimeVal: 1,
        startTime: noDataMarker,
        endTimeVal: 1,
        endTime: noDataMarker,
        type: noDataMarker,
    };
};

export const getUiEvent = (event: Event): UiEvent => {
    const uiEvent = createEmptyUiEvent();

    if (!event) {
        return uiEvent;
    }

    uiEvent.id = event.id;
    uiEvent.name = event.name;
    uiEvent.startTimeVal = event.startTime;
    uiEvent.startTime = formatDateAndTimeV1(event.startTime);
    uiEvent.endTimeVal = event.endTime;
    uiEvent.endTime = formatDateAndTimeV1(event.endTime);

    if (event.type) {
        uiEvent.type = event.type;
    }

    return uiEvent;
};

export const getUiEvents = (events: Event[]): UiEvent[] => {
    if (isEmpty(events)) {
        return [];
    }

    const uiEvents = [];
    for (const event of events) {
        uiEvents.push(getUiEvent(event));
    }

    return uiEvents;
};
