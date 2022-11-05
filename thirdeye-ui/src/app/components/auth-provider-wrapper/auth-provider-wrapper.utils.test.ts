/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    DUMMY_CLIENT_ID,
    isAuthDisabled,
    processAuthData,
} from "./auth-provider-wrapper.utils";

describe("Auth Provider Wrapper Utils", () => {
    describe("processAuthData", () => {
        it("should return empty string if authData is null", () => {
            const result = processAuthData(null);

            expect(result).toEqual("");
        });

        it("should return empty string if client id is undefined", () => {
            const result = processAuthData({});

            expect(result).toEqual("");
        });

        it("should return dummy value if auth is disabled", () => {
            const result = processAuthData({ authEnabled: false });

            expect(result).toEqual(DUMMY_CLIENT_ID);
        });

        it("should return clientId from payload if auth enabled", () => {
            const result = processAuthData({
                authEnabled: true,
                clientId: "hello-world",
            });

            expect(result).toEqual("hello-world");
        });

        it("should return empty string if auth enabled but client id missing", () => {
            const result = processAuthData({
                authEnabled: true,
            });

            expect(result).toEqual("");
        });
    });

    describe("isAuthDisabled", () => {
        it("should return false if authData is null", () => {
            expect(isAuthDisabled(null)).toEqual(false);
        });

        it("should return false if authEnabled exists and is true", () => {
            expect(isAuthDisabled({ authEnabled: true })).toEqual(false);
        });

        it("should return true if authEnabled exists and is false", () => {
            expect(isAuthDisabled({ authEnabled: false })).toEqual(true);
        });

        it("should return false if authEnabled does not exist in object", () => {
            expect(isAuthDisabled({})).toEqual(false);
        });
    });
});
