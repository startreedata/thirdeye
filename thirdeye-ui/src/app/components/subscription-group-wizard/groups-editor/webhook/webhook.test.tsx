import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import {
    SpecType,
    WebhookSpec,
} from "../../../../rest/dto/subscription-group.interfaces";
import { Webhook } from "./webhook.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("Webhook", () => {
    it("should render input with url", async () => {
        render(
            <Webhook
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={() => {
                    return;
                }}
                onSpecChange={() => {
                    return;
                }}
            />
        );

        expect(screen.getByDisplayValue(INITIAL_URL)).toBeInTheDocument();
    });

    it("should have called callback with the changed url value", async () => {
        const mockCallback = jest.fn();
        render(
            <Webhook
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={() => {
                    return;
                }}
                onSpecChange={mockCallback}
            />
        );

        // Verify valid state
        const nameInput = screen.getByDisplayValue(INITIAL_URL);

        expect(nameInput).toBeInTheDocument();

        // Change value
        fireEvent.change(nameInput, { target: { value: NEXT_URL } });

        // Verify callback is called
        expect(mockCallback).toHaveBeenCalledWith({
            type: "webhook",
            params: {
                url: NEXT_URL,
            },
        });
    });

    it("should have called delete if button is clicked", async () => {
        const mockCallback = jest.fn();
        render(
            <Webhook
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={mockCallback}
                onSpecChange={() => {
                    return;
                }}
            />
        );

        const deleteBtn = screen.getByTestId("webhook-delete-btn");

        fireEvent.click(deleteBtn);

        expect(mockCallback).toHaveBeenCalledTimes(1);
    });
});

const INITIAL_URL = "https://hello-world.com";
const NEXT_URL = "https://hooks.slack.com/";
const MOCK_CONFIGURATION: WebhookSpec = {
    type: SpecType.Webhook,
    params: {
        url: INITIAL_URL,
    },
};
