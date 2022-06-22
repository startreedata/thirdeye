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
import { CreateAlertConfigurationSection } from "../../../../../pages/alerts-create-page/alerts-create-page.interfaces";
import { AlertDateTimeCronAdvance } from "./alert-date-time-cron-advance.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("AlertDateTimeCronAdvance", () => {
    it("should render error message if given invalid cron and colored error", async () => {
        const mockValidationCallback = jest.fn();

        render(
            <AlertDateTimeCronAdvance
                cron={INVALID_CRON}
                onCronChange={() => {
                    return;
                }}
                onValidationChange={mockValidationCallback}
            />
        );

        expect(
            screen.getByText("Unexpected end of expression")
        ).toBeInTheDocument();

        expect(screen.getByTestId("cron-input-label")).toHaveClass("Mui-error");
        expect(mockValidationCallback).toHaveBeenCalledWith(
            CreateAlertConfigurationSection.CRON,
            false
        );
    });

    it("should remove error message if input valid cron and callback function called", async () => {
        const mockCallback = jest.fn();
        const mockValidationCallback = jest.fn();
        render(
            <AlertDateTimeCronAdvance
                cron={INVALID_CRON}
                onCronChange={mockCallback}
                onValidationChange={mockValidationCallback}
            />
        );

        // Verify error state
        expect(
            screen.getByText("Unexpected end of expression")
        ).toBeInTheDocument();

        expect(screen.getByTestId("cron-input-label")).toHaveClass("Mui-error");

        // Enter valid cron
        const cronInput = screen.getByDisplayValue(INVALID_CRON);
        fireEvent.change(cronInput, { target: { value: VALID_CRON } });

        expect(screen.getByTestId("cron-input-label")).not.toHaveClass(
            "Mui-error"
        );

        expect(
            screen.getByText(
                "At 0 minutes past the hour, every 2 hours, every day"
            )
        ).toBeInTheDocument();
        expect(mockCallback).toHaveBeenCalledWith(VALID_CRON);
        expect(mockValidationCallback).toHaveBeenCalledWith(
            CreateAlertConfigurationSection.CRON,
            true
        );
    });

    it("should render error message if input invalid cron and callback function is not called", async () => {
        const mockCallback = jest.fn();
        const mockValidationCallback = jest.fn();
        render(
            <AlertDateTimeCronAdvance
                cron={VALID_CRON}
                onCronChange={mockCallback}
                onValidationChange={mockValidationCallback}
            />
        );

        // Verify valid state
        expect(
            screen.getByText(
                "At 0 minutes past the hour, every 2 hours, every day"
            )
        ).toBeInTheDocument();

        expect(screen.getByTestId("cron-input-label")).not.toHaveClass(
            "Mui-error"
        );

        // Enter invalid cron
        const cronInput = screen.getByDisplayValue(VALID_CRON);
        fireEvent.change(cronInput, { target: { value: INVALID_CRON } });

        expect(screen.getByTestId("cron-input-label")).toHaveClass("Mui-error");

        expect(
            screen.getByText("Unexpected end of expression")
        ).toBeInTheDocument();
        expect(mockCallback).toHaveBeenCalledTimes(0);
        expect(mockValidationCallback).toHaveBeenCalledWith(
            CreateAlertConfigurationSection.CRON,
            false
        );
    });
});

const VALID_CRON = "0 0 0/2 1/1 * ? *";
const INVALID_CRON = "0 6 * *";
