/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
