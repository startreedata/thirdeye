// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import axios from "axios";
import { AlertTemplate } from "../dto/alert-template.interfaces";
import {
    createAlertTemplate,
    deleteAlertTemplate,
    getAlertTemplate,
    getAlertTemplates,
    updateAlertTemplate,
} from "./alert-templates.rest";

jest.mock("axios");

describe("Alert Templates REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getAlertTemplate should invoke axios.get with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAlertTemplateResponse,
        });

        await expect(getAlertTemplate(1)).resolves.toEqual(
            mockAlertTemplateResponse
        );

        expect(axios.get).toHaveBeenCalledWith("/api/alert-templates/1");
    });

    it("getAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlertTemplate(1)).rejects.toThrow("testError");
    });

    it("getAlertTemplates should invoke axios.get with appropriate input and return appropriate alerts", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAlertTemplateResponse],
        });

        await expect(getAlertTemplates()).resolves.toEqual([
            mockAlertTemplateResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith("/api/alert-templates");
    });

    it("getAlertTemplates should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlertTemplates()).rejects.toThrow("testError");
    });

    it("createAlertTemplate should invoke axios.post with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockAlertTemplateResponse],
        });

        await expect(
            createAlertTemplate(mockAlertTemplateRequest)
        ).resolves.toEqual(mockAlertTemplateResponse);

        expect(axios.post).toHaveBeenCalledWith("/api/alert-templates", [
            mockAlertTemplateRequest,
        ]);
    });

    it("createAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            createAlertTemplate(mockAlertTemplateRequest)
        ).rejects.toThrow("testError");
    });

    it("updateAlertTemplate should invoke axios.put with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockAlertTemplateResponse],
        });

        await expect(
            updateAlertTemplate(mockAlertTemplateRequest)
        ).resolves.toEqual(mockAlertTemplateResponse);

        expect(axios.put).toHaveBeenCalledWith("/api/alert-templates", [
            mockAlertTemplateRequest,
        ]);
    });

    it("updateAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(
            updateAlertTemplate(mockAlertTemplateRequest)
        ).rejects.toThrow("testError");
    });

    it("deleteAlertTemplate should invoke axios.delete with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAlertTemplateResponse,
        });

        await expect(deleteAlertTemplate(1)).resolves.toEqual(
            mockAlertTemplateResponse
        );

        expect(axios.delete).toHaveBeenCalledWith("/api/alert-templates/1");
    });

    it("deleteAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAlertTemplate(1)).rejects.toThrow("testError");
    });
});

const mockAlertTemplateRequest = {
    name: "testNameAlertRequest",
} as AlertTemplate;

const mockAlertTemplateResponse = {
    name: "testNameAlertResponse",
};

const mockError = new Error("testError");
