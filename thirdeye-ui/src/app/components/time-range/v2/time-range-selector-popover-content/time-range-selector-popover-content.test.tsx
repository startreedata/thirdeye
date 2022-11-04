import { createTheme, MuiThemeProvider } from "@material-ui/core";
import { render, screen } from "@testing-library/react";
import React from "react";
import { TimeRangeSelectorPopoverContent } from "./time-range-selector-popover-content.component";

const theme = createTheme({
    props: { MuiWithWidth: { initialWidth: "xl" } },
});

// Mock i18next so that src/app/utils/time-range/time-range.util.ts doesn't break
jest.mock("i18next", () => ({
    t(toTranslate: string): string {
        return toTranslate;
    },
}));

it("TimeRangeSelectorPopoverContent updates internal timeRangeDuration when external one changes", () => {
    const mockOnChange = jest.fn();
    let timeRangeDuration = JAN_TIME_RANGE_DURATION;
    const { rerender } = render(
        <MuiThemeProvider theme={theme}>
            <TimeRangeSelectorPopoverContent
                end={timeRangeDuration.endTime}
                start={timeRangeDuration.startTime}
                onChange={mockOnChange}
            />
        </MuiThemeProvider>
    );

    const applyButton = screen.getByText("label.apply");
    applyButton.click();

    expect(mockOnChange).toHaveBeenLastCalledWith(
        JAN_TIME_RANGE_DURATION.startTime,
        JAN_TIME_RANGE_DURATION.endTime
    );

    timeRangeDuration = FEB_TIME_RANGE_DURATION;
    rerender(
        <MuiThemeProvider theme={theme}>
            <TimeRangeSelectorPopoverContent
                end={timeRangeDuration.endTime}
                start={timeRangeDuration.startTime}
                onChange={mockOnChange}
            />
        </MuiThemeProvider>
    );
    applyButton.click();

    expect(mockOnChange).toHaveBeenLastCalledWith(
        FEB_TIME_RANGE_DURATION.startTime,
        FEB_TIME_RANGE_DURATION.endTime
    );
});

it("TimeRangeSelectorPopoverContent should change selected value when quick selection is clicked", () => {
    const mockOnChange = jest.fn();

    render(
        <MuiThemeProvider theme={theme}>
            <TimeRangeSelectorPopoverContent
                end={JAN_TIME_RANGE_DURATION.endTime}
                start={JAN_TIME_RANGE_DURATION.startTime}
                onChange={mockOnChange}
            />
        </MuiThemeProvider>
    );

    const applyButton = screen.getByText("label.apply");
    applyButton.click();

    // Make sure initial conditions are correct
    expect(mockOnChange).toHaveBeenLastCalledWith(
        JAN_TIME_RANGE_DURATION.startTime,
        JAN_TIME_RANGE_DURATION.endTime
    );

    const lastTwelveHoursButton = screen.getByText(/last-12-hours/);
    lastTwelveHoursButton.click();

    applyButton.click();

    expect(mockOnChange).not.toHaveBeenLastCalledWith(
        JAN_TIME_RANGE_DURATION.startTime,
        JAN_TIME_RANGE_DURATION.endTime
    );
});

it("TimeRangeSelectorPopoverContent should call onClose if passed", () => {
    const mockOnClose = jest.fn();

    render(
        <MuiThemeProvider theme={theme}>
            <TimeRangeSelectorPopoverContent
                end={JAN_TIME_RANGE_DURATION.endTime}
                start={JAN_TIME_RANGE_DURATION.startTime}
                onClose={mockOnClose}
            />
        </MuiThemeProvider>
    );

    const cancelButton = screen.getByText("label.cancel");
    cancelButton.click();

    expect(mockOnClose).toHaveBeenCalled();
});

const JAN_TIME_RANGE_DURATION = {
    // Saturday, January 2, 2021 12:00:00 AM (GMT)
    startTime: 1609545600000,
    // Thursday, January 21, 2021 12:00:00 AM (GMT)
    endTime: 1611187200000,
};

const FEB_TIME_RANGE_DURATION = {
    // Saturday, February 20, 2021 12:00:00 AM (GMT)
    startTime: 1613779200000,
    // February 27, 2021 12:00:00 AM (GMT)
    endTime: 1614384000000,
};
