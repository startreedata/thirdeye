/*
 * Copyright 2023 StarTree Inc
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

import { generateCustomErrorMessage } from "./errors.utils";

describe("Errors util", () => {
    it("generateCustomErrorMessage should replace error messages with custom messages", () => {
        const customErrorMessages = [
            {
                criteriaFunction: (message: string) =>
                    message.includes("error"),
                customMessage: "custom error message",
            },
        ];

        const errorMessages = ["error message"];

        expect(
            generateCustomErrorMessage(errorMessages, customErrorMessages)
        ).toEqual(["custom error message"]);
    });

    it("generateCustomErrorMessage should not replace error messages with custom messages", () => {
        const customErrorMessages = [
            {
                criteriaFunction: (message: string) =>
                    message.includes("error"),
                customMessage: "custom error message",
            },
        ];

        const errorMessages = ["message"];

        expect(
            generateCustomErrorMessage(errorMessages, customErrorMessages)
        ).toEqual(["message"]);
    });
});
