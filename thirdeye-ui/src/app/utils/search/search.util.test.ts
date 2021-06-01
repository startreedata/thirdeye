import i18n from "i18next";
import {
    deepSearchStringProperty,
    getSearchStatusLabel,
    getSelectedStatusLabel,
} from "./search.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("../number/number.util", () => ({
    formatNumber: jest.fn().mockImplementation((num) => num.toString()),
}));

describe("Search Util", () => {
    it("deepSearchStringProperty should return null for invalid object", () => {
        expect(deepSearchStringProperty(null, mockMatchFn)).toBeNull();
    });

    it("deepSearchStringProperty should return null for empty object", () => {
        expect(deepSearchStringProperty({}, mockMatchFn)).toBeNull();
    });

    it("deepSearchStringProperty should return null for primitive", () => {
        expect(deepSearchStringProperty(1, mockMatchFn)).toBeNull();
        expect(deepSearchStringProperty("testString", mockMatchFn)).toBeNull();
    });

    it("deepSearchStringProperty should return appropriate string property for object", () => {
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

    it("deepSearchStringProperty should invoke match function on all string properties of object", () => {
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

    it("deepSearchStringProperty should invoke match function on all string properties of object until it returns true", () => {
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

    it("getSearchStatusLabel should return appropriate search status label for invalid count and total", () => {
        expect(getSearchStatusLabel((null as unknown) as number, 1)).toEqual(
            "label.search-count"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.search-count", {
            count: "0",
            total: "1",
        });
        expect(getSearchStatusLabel(1, (null as unknown) as number)).toEqual(
            "label.search-count"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.search-count", {
            count: "1",
            total: "0",
        });
        expect(
            getSearchStatusLabel(
                (null as unknown) as number,
                (null as unknown) as number
            )
        ).toEqual("label.search-count");
        expect(i18n.t).toHaveBeenCalledWith("label.search-count", {
            count: "0",
            total: "0",
        });
    });

    it("getSearchStatusLabel should return appropriate search status label for count and total", () => {
        expect(getSearchStatusLabel(0, 1)).toEqual("label.search-count");
        expect(i18n.t).toHaveBeenCalledWith("label.search-count", {
            count: "0",
            total: "1",
        });
    });

    it("getSelectedStatusLabel should return appropriate selected status label for invalid count", () => {
        expect(getSelectedStatusLabel((null as unknown) as number)).toEqual(
            "label.selected-count"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.selected-count", {
            count: "0",
        });
    });

    it("getSelectedStatusLabel should return appropriate selected status label for count", () => {
        expect(getSelectedStatusLabel(1)).toEqual("label.selected-count");
        expect(i18n.t).toHaveBeenCalledWith("label.selected-count", {
            count: "1",
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
            numberProperty: 7,
            stringProperty: "testStringProperty3",
            booleanProperty: true,
            numberArrayProperty: [8, 9],
            stringArrayProperty: [
                "testStringArrayProperty5",
                "testStringArrayProperty6",
            ],
        },
    },
};
