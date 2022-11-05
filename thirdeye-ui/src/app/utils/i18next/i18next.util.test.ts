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
import enUS from "../../locale/languages/en-us.json";
import { getInitOptions } from "./i18next.util";

describe("i18next Util", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getInitOptions should return appropriate options", () => {
        jest.spyOn(console, "error").mockImplementation();
        const initOptions = getInitOptions();
        // Also invoke the missing key handler
        initOptions.missingKeyHandler &&
            initOptions.missingKeyHandler([""], "ns", "testKey", "");

        expect(initOptions.supportedLngs).toEqual(["en-US"]);
        expect(
            initOptions.resources && initOptions.resources["en-US"].translation
        ).toEqual(enUS);
        expect(initOptions.fallbackLng).toEqual(["en-US"]);
        expect(
            initOptions.interpolation && initOptions.interpolation.escapeValue
        ).toBeFalsy();
        expect(console.error).toHaveBeenCalledWith(
            `i18next: key not found "testKey"`
        );
        expect(initOptions.saveMissing).toBeTruthy();
    });
});
