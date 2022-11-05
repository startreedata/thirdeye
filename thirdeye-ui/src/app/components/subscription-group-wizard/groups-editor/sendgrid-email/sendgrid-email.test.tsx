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
import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import {
    SendgridEmailSpec,
    SpecType,
} from "../../../../rest/dto/subscription-group.interfaces";
import { SendgridEmail } from "./sendgrid-email.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("SendgridEmail", () => {
    it("should render inputs with initial values", async () => {
        render(
            <SendgridEmail
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={() => {
                    return;
                }}
                onSpecChange={() => {
                    return;
                }}
            />
        );

        expect(screen.getByDisplayValue("apiKey")).toBeInTheDocument();
        expect(
            screen.getByDisplayValue("thirdeye-alerts@startree.ai")
        ).toBeInTheDocument();
        expect(screen.getByText("helloworld@startree.ai")).toBeInTheDocument();
    });

    it("should have called callback with the changed api key value", async () => {
        const mockCallback = jest.fn();
        render(
            <SendgridEmail
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={() => {
                    return;
                }}
                onSpecChange={mockCallback}
            />
        );

        // Verify valid state
        const apiKeyInput = screen.getByDisplayValue("apiKey");

        expect(apiKeyInput).toBeInTheDocument();

        // Change value
        fireEvent.change(apiKeyInput, { target: { value: "anotherKey" } });

        // Verify callback is called
        expect(mockCallback).toHaveBeenCalledWith({
            type: "email-sendgrid",
            params: {
                apiKey: "anotherKey",
                emailRecipients: {
                    from: "thirdeye-alerts@startree.ai",
                    to: ["helloworld@startree.ai"],
                },
            },
        });
    });

    it("should have called callback with the changed `from` value", async () => {
        const mockCallback = jest.fn();
        render(
            <SendgridEmail
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={() => {
                    return;
                }}
                onSpecChange={mockCallback}
            />
        );

        // Verify valid state
        const fromInput = screen.getByDisplayValue(
            "thirdeye-alerts@startree.ai"
        );

        expect(fromInput).toBeInTheDocument();

        // Change value
        fireEvent.change(fromInput, {
            target: { value: "anotherEmail@email.com" },
        });

        // Verify callback is called
        expect(mockCallback).toHaveBeenCalledWith({
            type: "email-sendgrid",
            params: {
                apiKey: "apiKey",
                emailRecipients: {
                    from: "anotherEmail@email.com",
                    to: ["helloworld@startree.ai"],
                },
            },
        });
    });

    it("should have called delete if button is clicked", async () => {
        const mockCallback = jest.fn();
        render(
            <SendgridEmail
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={mockCallback}
                onSpecChange={() => {
                    return;
                }}
            />
        );

        const deleteBtn = screen.getByTestId("email-delete-btn");

        fireEvent.click(deleteBtn);

        expect(mockCallback).toHaveBeenCalledTimes(1);
    });
});

const MOCK_CONFIGURATION: SendgridEmailSpec = {
    type: SpecType.EmailSendgrid,
    params: {
        apiKey: "apiKey",
        emailRecipients: {
            from: "thirdeye-alerts@startree.ai",
            to: ["helloworld@startree.ai"],
        },
    },
};
