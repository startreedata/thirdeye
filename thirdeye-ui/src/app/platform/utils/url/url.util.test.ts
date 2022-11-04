import {
    addLeadingForwardSlashV1,
    addTrailingForwardSlashV1,
    removeLeadingForwardSlashV1,
    removeTrailingForwardSlashV1,
} from "./url.util";

describe("URL Util", () => {
    it("addLeadingForwardSlashV1 should return appropriate string for invalid url", () => {
        expect(addLeadingForwardSlashV1(null as unknown as string)).toEqual(
            "/"
        );
    });

    it("addLeadingForwardSlashV1 should return appropriate string for empty url", () => {
        expect(addLeadingForwardSlashV1("")).toEqual("/");
    });

    it("addLeadingForwardSlashV1 should return appropriate string for url", () => {
        expect(addLeadingForwardSlashV1("localhost")).toEqual("/localhost");
        expect(addLeadingForwardSlashV1("/localhost")).toEqual("/localhost");
    });

    it("removeLeadingForwardSlashV1 should return appropriate string for invalid url", () => {
        expect(removeLeadingForwardSlashV1(null as unknown as string)).toEqual(
            ""
        );
    });

    it("removeLeadingForwardSlashV1 should return appropriate string for empty url", () => {
        expect(removeLeadingForwardSlashV1("")).toEqual("");
    });

    it("removeLeadingForwardSlashV1 should return appropriate string for url", () => {
        expect(removeLeadingForwardSlashV1("localhost")).toEqual("localhost");
        expect(removeLeadingForwardSlashV1("/localhost")).toEqual("localhost");
    });

    it("addTrailingForwardSlashV1 should return appropriate string for invalid url", () => {
        expect(addTrailingForwardSlashV1(null as unknown as string)).toEqual(
            "/"
        );
    });

    it("addTrailingForwardSlashV1 should return appropriate string for empty url", () => {
        expect(addTrailingForwardSlashV1("")).toEqual("/");
    });

    it("addTrailingForwardSlashV1 should return appropriate string for url", () => {
        expect(addTrailingForwardSlashV1("http://localhost:8080")).toEqual(
            "http://localhost:8080/"
        );
        expect(addTrailingForwardSlashV1("http://localhost:8080/")).toEqual(
            "http://localhost:8080/"
        );
    });

    it("removeTrailingForwardSlashV1 should return appropriate string for invalid url", () => {
        expect(removeTrailingForwardSlashV1(null as unknown as string)).toEqual(
            ""
        );
    });

    it("removeTrailingForwardSlashV1 should return appropriate string for empty url", () => {
        expect(removeTrailingForwardSlashV1("")).toEqual("");
    });

    it("removeTrailingForwardSlashV1 should return appropriate string for url", () => {
        expect(removeTrailingForwardSlashV1("http://localhost:8080")).toEqual(
            "http://localhost:8080"
        );
        expect(removeTrailingForwardSlashV1("http://localhost:8080/")).toEqual(
            "http://localhost:8080"
        );
    });
});
