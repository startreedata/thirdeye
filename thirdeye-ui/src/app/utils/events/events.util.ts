import { flattenDeep, get, isNil, map, uniq } from "lodash";
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

export const handleEventsSearch = (
    searchValue: string,
    events: Event[] | null,
    searchDataKeys: string[]
): Event[] => {
    let updatedEvents: Event[];
    const filteredRowKeyValues = new Set();
    if (!events) {
        updatedEvents = [];
    } else if (!searchValue) {
        updatedEvents = [...events];
    } else {
        updatedEvents = [];
        for (const eachSearchDataKey of searchDataKeys) {
            for (const eachData of events) {
                const rowKeyValue = get(eachData, "id");
                if (filteredRowKeyValues.has(rowKeyValue)) {
                    // Row already filtered
                    continue;
                }

                // Get data at search key
                const searchKeyValue = get(eachData, eachSearchDataKey);
                if (
                    isNil(searchKeyValue) ||
                    (typeof searchKeyValue !== "string" &&
                        typeof searchKeyValue !== "number" &&
                        typeof searchKeyValue !== "boolean" &&
                        !Array.isArray(searchKeyValue))
                ) {
                    // Skip searching
                    continue;
                }

                if (
                    searchKeyValue
                        .toString()
                        .toLocaleLowerCase()
                        .includes(searchValue.toLocaleLowerCase().trim())
                ) {
                    // Match found
                    filteredRowKeyValues.add(rowKeyValue);
                    updatedEvents.push(eachData);
                }
            }
        }
    }

    return updatedEvents;
};
