import i18n from "i18next";
import { Event } from "../../rest/dto/event.interfaces";

export const createEmptyEvent = (): Event => {
    return {
        name: "",
        type: "",
        startTime: Date.now(),
        endTime: Date.now(),
    } as Event;
};
