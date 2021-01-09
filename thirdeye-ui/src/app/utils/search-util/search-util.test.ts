import { deepSearchStringProperty } from "./search-util";

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

    test("deepSearchStringProperty should invoke match function on all string properties of object", () => {
        deepSearchStringProperty(mockObject, mockMatchFn);

        expect(mockMatchFn).toHaveBeenCalledTimes(9);
        expect(mockMatchFn).toHaveBeenNthCalledWith(1, "testString1");
        expect(mockMatchFn).toHaveBeenNthCalledWith(2, "testString2");
        expect(mockMatchFn).toHaveBeenNthCalledWith(3, "testString3");
        expect(mockMatchFn).toHaveBeenNthCalledWith(4, "testString4");
        expect(mockMatchFn).toHaveBeenNthCalledWith(5, "testString5");
        expect(mockMatchFn).toHaveBeenNthCalledWith(6, "testString6");
        expect(mockMatchFn).toHaveBeenNthCalledWith(7, "testString7");
        expect(mockMatchFn).toHaveBeenNthCalledWith(8, "testString8");
        expect(mockMatchFn).toHaveBeenNthCalledWith(9, "testString9");
    });

    test("deepSearchStringProperty should invoke match function on all string properties of object until it returns true", () => {
        mockMatchFn
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(true);
        deepSearchStringProperty(mockObject, mockMatchFn);

        expect(mockMatchFn).toHaveBeenCalledTimes(3);
        expect(mockMatchFn).toHaveBeenNthCalledWith(1, "testString1");
        expect(mockMatchFn).toHaveBeenNthCalledWith(2, "testString2");
        expect(mockMatchFn).toHaveBeenNthCalledWith(3, "testString3");
    });
});

const mockMatchFn = jest.fn();

const mockObject = {
    numberProperty: 1,
    stringProperty: "testString1",
    booleanProperty: true,
    numberArrayProperty: [2, 3],
    stringArrayProperty: ["testString2", "testString3"],
    objectProperty: {
        numberProperty: 4,
        stringProperty: "testString4",
        booleanProperty: true,
        numberArrayProperty: [5, 6],
        stringArrayProperty: ["testString5", "testString6"],
    },
    nestedObjectProperty: {
        objectProperty: {
            numberProperty: 5,
            stringProperty: "testString7",
            booleanProperty: true,
            numberArrayProperty: [6, 7],
            stringArrayProperty: ["testString8", "testString9"],
        },
    },
};
