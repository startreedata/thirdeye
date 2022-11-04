import { NormalizedSeries } from "../time-series-chart.interfaces";
import { sortSeries } from "./legend.utils";

describe("Legend Util", () => {
    it("sortSeries should return correct order when all have legendIndex", () => {
        const input = [
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "2",
                legendIndex: 2,
            },
            {
                name: "3",
                legendIndex: 4,
            },
            {
                name: "4",
                legendIndex: 1,
            },
        ] as NormalizedSeries[];

        expect(sortSeries(input)).toEqual([
            {
                name: "4",
                legendIndex: 1,
            },
            {
                name: "2",
                legendIndex: 2,
            },
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "3",
                legendIndex: 4,
            },
        ]);
    });

    it("sortSeries should return correct order when mixed legendIndex maintaining initial sort order", () => {
        const input = [
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "2",
            },
            {
                name: "3",
                legendIndex: 4,
            },
            {
                name: "4",
            },
        ] as NormalizedSeries[];

        expect(sortSeries(input)).toEqual([
            {
                name: "2",
            },
            {
                name: "4",
            },
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "3",
                legendIndex: 4,
            },
        ]);
    });

    it("sortSeries should maintain sort order when legendIndex does not exist", () => {
        const input = [
            {
                name: "1",
            },
            {
                name: "a2",
            },
            {
                name: "z3",
            },
        ] as NormalizedSeries[];

        expect(sortSeries(input)).toEqual([
            {
                name: "1",
            },
            {
                name: "a2",
            },
            {
                name: "z3",
            },
        ]);
    });
});
