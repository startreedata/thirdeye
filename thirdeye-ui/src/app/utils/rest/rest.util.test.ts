// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import { AxiosError } from "axios";
import { getErrorMessages } from "./rest.util";

describe("rest util", () => {
    it("should return empty array for null value as input", () => {
        expect(getErrorMessages(null as unknown as AxiosError)).toStrictEqual(
            []
        );
    });

    it("should return empty array for empty object as input", () => {
        expect(getErrorMessages({} as AxiosError)).toStrictEqual([]);
    });

    it("should return empty array for empty response value as input", () => {
        expect(getErrorMessages({ response: {} } as AxiosError)).toStrictEqual(
            []
        );
    });

    it("should return empty array for empty response data value as input", () => {
        expect(
            getErrorMessages({ response: { data: {} } } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return empty array for empty errors array", () => {
        expect(
            getErrorMessages({ response: { data: { list: [] } } } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return empty array for list with empty error object", () => {
        expect(
            getErrorMessages({
                response: { data: { list: [{}] } },
            } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return empty array for error without msg property", () => {
        expect(
            getErrorMessages({
                response: { data: { list: [{ code: "testCode" }] } },
            } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return errors from list of errors object", () => {
        expect(
            getErrorMessages({
                response: {
                    data: { list: [{ code: "testCode", msg: "testMessage" }] },
                },
            } as AxiosError)
        ).toStrictEqual(["testMessage"]);
    });

    it("should return errors from list of errors object even if code is missing", () => {
        expect(
            getErrorMessages({
                response: {
                    data: { list: [{ msg: "testMessage" }] },
                },
            } as AxiosError)
        ).toStrictEqual(["testMessage"]);
    });

    it("should return all the errors from list of errors object", () => {
        expect(
            getErrorMessages({
                response: {
                    data: {
                        list: [
                            { code: "testCode1", msg: "testMessage1" },
                            { code: "testCode2", msg: "testMessage2" },
                            { code: "testCode3", msg: "testMessage3" },
                        ],
                    },
                },
            } as AxiosError)
        ).toStrictEqual(["testMessage1", "testMessage2", "testMessage3"]);
    });
});
