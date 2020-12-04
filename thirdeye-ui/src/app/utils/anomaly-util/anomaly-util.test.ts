import i18n from "i18next";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { getAnomalyName } from "./anomaly-util";

jest.mock("i18next");

const mockAnomaly = {
    id: 1,
} as Anomaly;

describe("Anomaly Util", () => {
    beforeAll(() => {
        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getAnomalyName shall return no data available string for an invalid anomaly", () => {
        const name = getAnomalyName((null as unknown) as Anomaly);

        expect(name).toEqual("label.no-data-available-marker");
    });

    test("getAnomalyName shall return anomaly name for a valid anomaly", () => {
        const name = getAnomalyName(mockAnomaly);

        expect(name).toEqual("label.anomaly label.anomaly-id");
        expect(i18n.t).toHaveBeenCalledWith("label.anomaly-id", { id: 1 });
    });
});
