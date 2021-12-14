import { getClientIdFromUrl } from "./client-id.util";

describe("Get Client ID Util (getClientIdFromUrl)", () => {
    it("should return null if passed null", () => {
        expect(getClientIdFromUrl((null as unknown) as string)).toBeNull();
    });

    it("should return null if passed empty string", () => {
        expect(getClientIdFromUrl("")).toBeNull();
    });

    it("should return null if url is missing http", () => {
        expect(getClientIdFromUrl("localhost:1755")).toBeNull();
    });

    it("should return null if passed localhost with http", () => {
        expect(getClientIdFromUrl("http://localhost:1755")).toBeNull();
    });

    it("should return {namespace}-{deploymentId} for a production url", () => {
        expect(
            getClientIdFromUrl(
                "http://thirdeye-tv592d4w.te.192.168.64.37.nip.io:30000"
            )
        ).toEqual("te-thirdeye-tv592d4w");
    });

    it("should return expected client id if passed valid url", () => {
        expect(
            getClientIdFromUrl(
                "http://third-eye.aws.startree-dev.startree.cloud"
            )
        ).toEqual("aws-third-eye");
    });
});
