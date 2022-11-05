import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { AlertDateTimeCronAdvance } from "./alert-date-time-cron-advance.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("AlertDateTimeCronAdvance", () => {
    it("should render error message if given invalid cron and colored error", async () => {
        render(
            <AlertDateTimeCronAdvance
                cron={INVALID_CRON}
                onCronChange={() => {
                    return;
                }}
            />
        );

        expect(
            screen.getByTestId("error-message-container")
        ).toBeInTheDocument();

        expect(screen.getByTestId("cron-input-label")).toHaveClass("Mui-error");
    });

    it("should remove error message if input valid cron and callback function called", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertDateTimeCronAdvance
                cron={INVALID_CRON}
                onCronChange={mockCallback}
            />
        );

        // Verify error state
        expect(
            screen.getByTestId("error-message-container")
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
    });

    it("should render error message if input invalid cron and callback function is called", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertDateTimeCronAdvance
                cron={VALID_CRON}
                onCronChange={mockCallback}
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

        expect(screen.getByTestId("cron-input-label")).toBeInTheDocument();
        expect(mockCallback).toHaveBeenCalledTimes(1);
    });
});

const VALID_CRON = "0 0 0/2 1/1 * ? *";
const INVALID_CRON = "0 6 * *";
