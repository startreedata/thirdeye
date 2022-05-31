import i18n from "i18next";
import { Event } from "../../rest/dto/event.interfaces";

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
