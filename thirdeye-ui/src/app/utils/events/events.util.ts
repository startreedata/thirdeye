import { flattenDeep, map, uniq } from "lodash";
import { EditableEvent, Event } from "../../rest/dto/event.interfaces";
import { generateDateRangeMonthsFromNow } from "../routes/routes.util";

export const createEmptyEvent = (): EditableEvent => {
    const [start, end] = generateDateRangeMonthsFromNow(0);

    return {
        name: "",
        startTime: start,
        endTime: end,
        type: "",
    };
};

export const getSearchDataKeysForEvents = (events: Event[]): string[] => {
    return [
        "name",
        "type",
        "startTime",
        "endTime",
        // Extract keys from targetDimensionMap to allow search over map
        ...uniq(
            flattenDeep(
                events.map((event) =>
                    map(
                        event.targetDimensionMap,
                        (_value: string[], key: string) =>
                            `targetDimensionMap.${key}`
                    )
                )
            )
        ),
    ];
};
