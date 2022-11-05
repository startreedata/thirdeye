import {
    baselineOffsetToMilliseconds,
    comparisonOffsetReadableValue,
} from "./anomaly-breakdown.util";

describe("Anomaly Breakdown Util", () => {
    it("comparisonOffsetReadableValue should return expected human string string", () => {
        expect(comparisonOffsetReadableValue("P1D")).toEqual("1 day ago");
        expect(comparisonOffsetReadableValue("P2D")).toEqual("2 days ago");
        expect(comparisonOffsetReadableValue("P4Y")).toEqual("4 years ago");
    });

    it("comparisonOffsetReadableValue should return error string if unparseable", () => {
        expect(comparisonOffsetReadableValue("PD")).toEqual(
            "could not parse offset"
        );
        expect(comparisonOffsetReadableValue("P2L")).toEqual(
            "could not parse offset"
        );
    });

    it("baselineOffsetToMilliseconds should return milliseconds", () => {
        expect(baselineOffsetToMilliseconds("P1D")).toEqual(86400000);
        expect(baselineOffsetToMilliseconds("P2D")).toEqual(172800000);
        expect(baselineOffsetToMilliseconds("P4Y")).toEqual(126230400000);
    });

    it("baselineOffsetToMilliseconds should return 0 if unreadable", () => {
        expect(baselineOffsetToMilliseconds("PD")).toEqual(0);
        expect(baselineOffsetToMilliseconds("P2L")).toEqual(0);
    });
});
