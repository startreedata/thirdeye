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
import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { AlertDetails } from "./alert-details.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("AlertDetails", () => {
    it("should render name and description from passed alert", async () => {
        render(
            <AlertDetails
                alert={MOCK_ALERT}
                onAlertPropertyChange={() => {
                    return;
                }}
            />
        );

        expect(screen.getByDisplayValue("hello-world")).toBeInTheDocument();
        expect(screen.getByDisplayValue("foo bar")).toBeInTheDocument();
    });

    it("should render error state if name is empty and should not have called callback", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertDetails
                alert={MOCK_ALERT}
                onAlertPropertyChange={mockCallback}
            />
        );

        // Verify valid state
        const nameInput = screen.getByDisplayValue("hello-world");

        expect(nameInput).toBeInTheDocument();
        expect(screen.getByDisplayValue("foo bar")).toBeInTheDocument();

        // Enter empty name
        fireEvent.change(nameInput, { target: { value: "" } });

        expect(screen.getByTestId("name-input-label")).toHaveClass("Mui-error");

        expect(
            screen.getByText("message.please-enter-valid-name")
        ).toBeInTheDocument();
        expect(mockCallback).toHaveBeenCalledTimes(0);
    });

    it("should have called callback if valid input for name and description", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertDetails
                alert={MOCK_ALERT}
                onAlertPropertyChange={mockCallback}
            />
        );

        // Verify name
        const nameInput = screen.getByDisplayValue("hello-world");

        fireEvent.change(nameInput, { target: { value: "new-value" } });

        expect(mockCallback).toHaveBeenCalledWith({ name: "new-value" });

        // Verify description
        const descriptionInput = screen.getByDisplayValue("foo bar");

        fireEvent.change(descriptionInput, {
            target: { value: "new-value-123" },
        });

        expect(mockCallback).toHaveBeenCalledWith({
            description: "new-value-123",
        });
    });
});

const MOCK_ALERT: EditableAlert = {
    name: "hello-world",
    description: "foo bar",
    cron: "",
    templateProperties: {},
};
