import {
    getAlertsAllPath,
    getAlertsCreatePath,
    getAlertsDetailPath,
    getAlertsPath,
    getAlertsUpdatePath,
    getAnomaliesAllPath,
    getAnomaliesDetailPath,
    getAnomaliesPath,
    getBasePath,
    getHomePath,
    getPageNotFoundPath,
    getSignInPath,
    getSignOutPath,
} from "./routes.util";

describe("Routes Util", () => {
    test("getBasePath shall return appropriate path", () => {
        expect(getBasePath()).toEqual("/");
    });

    test("getHomePath shall return appropriate path", () => {
        expect(getHomePath()).toEqual("/home");
    });

    test("getAlertsPath shall return appropriate path", () => {
        expect(getAlertsPath()).toEqual("/alerts");
    });

    test("getAlertsAllPath shall return appropriate path", () => {
        expect(getAlertsAllPath()).toEqual("/alerts/all");
    });

    test("getAlertsDetailPath shall return appropriate path", () => {
        expect(getAlertsDetailPath(1)).toEqual("/alerts/id/1");
    });

    test("getAlertsCreatePath shall return appropriate path", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create");
    });

    test("getAlertsUpdatePath shall return appropriate path", () => {
        expect(getAlertsUpdatePath(1)).toEqual("/alerts/update/id/1");
    });

    test("getAnomaliesPath shall return appropriate path", () => {
        expect(getAnomaliesPath()).toEqual("/anomalies");
    });

    test("getAnomaliesAllPath shall return appropriate path", () => {
        expect(getAnomaliesAllPath()).toEqual("/anomalies/all");
    });

    test("getAnomaliesDetailPath shall return appropriate path", () => {
        expect(getAnomaliesDetailPath(1)).toEqual("/anomalies/id/1");
    });

    test("getSignInPath shall return appropriate path", () => {
        expect(getSignInPath()).toEqual("/signIn");
    });

    test("getSignOutPath shall return appropriate path", () => {
        expect(getSignOutPath()).toEqual("/signOut");
    });

    test("getPageNotFoundPath shall return appropriate path", () => {
        expect(getPageNotFoundPath()).toEqual("/pageNotFound");
    });
});
