import i18n from "i18next";
import { deepSearchStringProperty, getSearchStatusLabel } from "./search.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("../number/number.util", () => ({
    formatNumber: jest.fn().mockImplementation((num) => num.toString()),
}));

describe("Search Util", () => {
    test("deepSearchStringProperty should return null for invalid object", () => {
        expect(deepSearchStringProperty(null, mockMatchFn)).toBeNull();
    });

    test("deepSearchStringProperty should return null for empty object", () => {
        expect(deepSearchStringProperty({}, mockMatchFn)).toBeNull();
    });

    test("deepSearchStringProperty should return null for primitive", () => {
        expect(deepSearchStringProperty(1, mockMatchFn)).toBeNull();
        expect(deepSearchStringProperty("testString", mockMatchFn)).toBeNull();
    });

    test("deepSearchStringProperty should return appropriate string property for object", () => {
        expect(
            deepSearchStringProperty(
                mockObject,
                (value) => value === "testString"
            )
        ).toBeNull();
        expect(
            deepSearchStringProperty(
                mockObject,
                (value) => value === "testStringArrayProperty4"
            )
        ).toEqual("testStringArrayProperty4");
    });

    test("deepSearchStringProperty should invoke match function on all string properties of object", () => {
        deepSearchStringProperty(mockObject, mockMatchFn);

        expect(mockMatchFn).toHaveBeenCalledTimes(9);
        expect(mockMatchFn).toHaveBeenNthCalledWith(1, "testStringProperty1");
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            2,
            "testStringArrayProperty1"
        );
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            3,
            "testStringArrayProperty2"
        );
        expect(mockMatchFn).toHaveBeenNthCalledWith(4, "testStringProperty2");
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            5,
            "testStringArrayProperty3"
        );
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            6,
            "testStringArrayProperty4"
        );
        expect(mockMatchFn).toHaveBeenNthCalledWith(7, "testStringProperty3");
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            8,
            "testStringArrayProperty5"
        );
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            9,
            "testStringArrayProperty6"
        );
    });

    test("deepSearchStringProperty should invoke match function on all string properties of object until it returns true", () => {
        mockMatchFn
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(true);
        deepSearchStringProperty(mockObject, mockMatchFn);

        expect(mockMatchFn).toHaveBeenCalledTimes(3);
        expect(mockMatchFn).toHaveBeenNthCalledWith(1, "testStringProperty1");
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            2,
            "testStringArrayProperty1"
        );
        expect(mockMatchFn).toHaveBeenNthCalledWith(
            3,
            "testStringArrayProperty2"
        );
    });

    test("getSearchStatusLabel should return appropriate search status label for count and total", () => {
        expect(getSearchStatusLabel(1, 2)).toEqual("label.search-count");
        expect(i18n.t).toHaveBeenCalledWith("label.search-count", {
            count: "1",
            total: "2",
        });
    });
});

const mockMatchFn = jest.fn();

const mockObject = {
    numberProperty: 1,
    stringProperty: "testStringProperty1",
    booleanProperty: true,
    numberArrayProperty: [2, 3],
    stringArrayProperty: [
        "testStringArrayProperty1",
        "testStringArrayProperty2",
    ],
    objectProperty: {
        numberProperty: 4,
        stringProperty: "testStringProperty2",
        booleanProperty: true,
        numberArrayProperty: [5, 6],
        stringArrayProperty: [
            "testStringArrayProperty3",
            "testStringArrayProperty4",
        ],
    },
    nestedObjectProperty: {
        objectProperty: {
            numberProperty: 5,
            stringProperty: "testStringProperty3",
            booleanProperty: true,
            numberArrayProperty: [6, 7],
            stringArrayProperty: [
                "testStringArrayProperty5",
                "testStringArrayProperty6",
            ],
        },
    },
};
