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
import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import {
    PagerDutySpec,
    SpecType,
} from "../../../../../../rest/dto/subscription-group.interfaces";
import { PagerDuty } from "./pager-duty.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("PagerDuty", () => {
    it("should render input with url", async () => {
        render(
            <PagerDuty
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={() => {
                    return;
                }}
                onSpecChange={() => {
                    return;
                }}
            />
        );

        expect(screen.getByDisplayValue(INITIAL_KEY)).toBeInTheDocument();
    });

    it("should have called callback with the changed eventsIntegrationKey value", async () => {
        const mockCallback = jest.fn();
        render(
            <PagerDuty
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={() => {
                    return;
                }}
                onSpecChange={mockCallback}
            />
        );

        // Verify valid state
        const nameInput = screen.getByDisplayValue(INITIAL_KEY);

        expect(nameInput).toBeInTheDocument();

        // Change value
        fireEvent.change(nameInput, { target: { value: NEXT_KEY } });

        // Verify callback is called
        expect(mockCallback).toHaveBeenCalledWith({
            type: "pagerduty",
            params: {
                eventsIntegrationKey: NEXT_KEY,
            },
        });
    });

    it("should have called delete if button is clicked", async () => {
        const mockCallback = jest.fn();
        render(
            <PagerDuty
                configuration={MOCK_CONFIGURATION}
                onDeleteClick={mockCallback}
                onSpecChange={() => {
                    return;
                }}
            />
        );

        const deleteBtn = screen.getByTestId("pager-duty-delete-btn");

        fireEvent.click(deleteBtn);

        expect(mockCallback).toHaveBeenCalledTimes(1);
    });
});

const INITIAL_KEY = "qwejgkldfhnasopvzxcmnbvkytrewqaz";
const NEXT_KEY = "hgfdsapoiuytrewqlkjhgfdscvbnmlkj";
const MOCK_CONFIGURATION: PagerDutySpec = {
    type: SpecType.PagerDuty,
    params: {
        eventsIntegrationKey: INITIAL_KEY,
    },
};
