import { getSearchDataKeysForEvents, handleEventsSearch } from "./events.util";

describe("Events util", () => {
    it("getSearchDataKeysForEvents should extract keys from maps to allow search over nested objects", () => {
        expect(getSearchDataKeysForEvents(mockEventData)).toEqual(
            mockDataKeysResult
        );
    });

    it("handleEventsSearch should handle search over nested objects", () => {
        const searchKey = "IT";

        expect(
            handleEventsSearch(searchKey, mockEventData, mockDataKeysResult)
        ).toEqual(searchCountryCodeResult);
    });

    it("handleEventsSearch should handle search over event name", () => {
        const searchKey = "go";

        expect(
            handleEventsSearch(searchKey, mockEventData, mockDataKeysResult)
        ).toEqual(searchNameResult);
    });

    it("handleEventsSearch should handle search over event type column", () => {
        const searchKey = "day";

        expect(
            handleEventsSearch(searchKey, mockEventTypeData, mockDataKeysResult)
        ).toEqual(searchTypeResult);
    });
});

const mockEventData = [
    {
        id: 1,
        name: "Ferragosto",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["IT"],
        },
    },
    {
        id: 2,
        name: "Harmony Day",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["AU"],
        },
    },
    {
        id: 3,
        name: "Otago Anniversary Day (Otago)",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["NZ"],
        },
    },
];

const mockEventTypeData = [
    {
        id: 1,
        name: "Ferragosto",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["IT"],
        },
    },
];

const mockDataKeysResult = [
    "name",
    "type",
    "startTime",
    "endTime",
    "targetDimensionMap.countryCode",
];

const searchCountryCodeResult = [
    {
        id: 1,
        name: "Ferragosto",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["IT"],
        },
    },
];

const searchNameResult = [
    {
        id: 1,
        name: "Ferragosto",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["IT"],
        },
    },
    {
        id: 3,
        name: "Otago Anniversary Day (Otago)",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["NZ"],
        },
    },
];

const searchTypeResult = [
    {
        id: 1,
        name: "Ferragosto",
        type: "HOLIDAY",
        startTime: -1,
        endTime: -1,
        targetDimensionMap: {
            countryCode: ["IT"],
        },
    },
];
