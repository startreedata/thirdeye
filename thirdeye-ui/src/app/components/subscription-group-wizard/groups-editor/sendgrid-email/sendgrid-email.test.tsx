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
