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
import {
    SlackSpec,
    SpecType,
} from "../../../../rest/dto/subscription-group.interfaces";
import { Slack } from "./slack.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("Slack", () => {
    it("should render input with webhook url", async () => {
        render(
            <Slack
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
            <Slack
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
            type: "slack",
            params: {
                webhookUrl: NEXT_URL,
            },
        });
    });

    it("should have called delete if button is clicked", async () => {
        const mockCallback = jest.fn();
        render(
            <Slack
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={mockCallback}
                onSpecChange={() => {
                    return;
                }}
            />
        );

        const deleteBtn = screen.getByTestId("slack-delete-btn");

        fireEvent.click(deleteBtn);

        expect(mockCallback).toHaveBeenCalledTimes(1);
    });
});

const INITIAL_URL = "https://hello-world.com";
const NEXT_URL = "https://hooks.slack.com/";
const MOCK_CONFIGURATION: SlackSpec = {
    type: SpecType.Slack,
    params: {
        webhookUrl: INITIAL_URL,
    },
};
