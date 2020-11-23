import {
    Box,
    Button,
    ButtonGroup,
    Card,
    Popover,
    Typography,
} from "@material-ui/core";
import { CalendarToday } from "@material-ui/icons";
import React, { FunctionComponent, useEffect } from "react";
import ReactDatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useTranslation } from "react-i18next";
import { useDateRangePickerStore } from "../../store/date-range-picker/date-range-picker-store";
import { DateRange } from "../../store/date-range-picker/date-range-picker-store.interfaces";
import { parseDateTime, subtractDate } from "../../utils/datetime/date-utils";
import { cardStyles } from "../styles/common.styles";
import { DatePickerProps } from "./date-range-picker.interfaces";
import { useDatePickerStyles } from "./date-range-picker.styles";

const currentDate = new Date();

// Default ranges
const RANGES: { [name: string]: [Date, Date] } = {
    Custom: [currentDate, currentDate],
    WEEK: [subtractDate(currentDate, 6, "days"), currentDate],
    MONTH: [subtractDate(currentDate, 29, "days"), currentDate],
    QUARTER: [subtractDate(currentDate, 5, "months"), currentDate],
    YEAR: [subtractDate(currentDate, 1, "year"), currentDate],
};

const DateRangePicker: FunctionComponent<DatePickerProps> = ({
    // If no ranges provided then set default one
    ranges = RANGES,
}: DatePickerProps) => {
    const [localDateRange, setLocalDateRange] = React.useState<DateRange>({
        from: currentDate,
        to: currentDate,
        predefineRangeName: "Custom",
    });
    const [isOpen, setIsOpen] = React.useState(false);
    const popupRef = React.useRef<HTMLButtonElement>(null);
    const rootRef = React.useRef<HTMLDivElement>(null);
    const datePickerClasses = useDatePickerStyles();
    const { t } = useTranslation();

    const [setDateRange, dateRange] = useDateRangePickerStore((state) => [
        state.setDateRange,
        state.dateRange,
    ]);
    const cardClasses = cardStyles();

    useEffect(() => {
        setLocalDateRange({
            from: dateRange.from || currentDate,
            to: dateRange.to || currentDate,
            predefineRangeName: dateRange.predefineRangeName || "Custom",
        });
    }, [dateRange]);

    const openPopUp = (): void => {
        setIsOpen(true);
    };

    const handleClickOutside = (e: MouseEvent): void => {
        if (
            rootRef &&
            rootRef.current &&
            rootRef.current.contains(e.target as Node)
        ) {
            // inside click
            return;
        }
        setIsOpen(false);
    };

    React.useEffect(() => {
        // add when mounted
        document.addEventListener("mousedown", handleClickOutside);

        // return function to be called when unmounted
        return (): void => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    return (
        <div>
            <Box alignItems="center" display="flex">
                <Typography variant="body2">
                    {parseDateTime(localDateRange.from, "h:mm a, MMM DD, 'YY")}{" "}
                    - {parseDateTime(localDateRange.to, "h:mm a, MMM DD, 'YY")}
                </Typography>
                <Button
                    className={datePickerClasses.buttonIcon}
                    color="primary"
                    ref={popupRef}
                    variant="outlined"
                    onClick={openPopUp}
                >
                    <CalendarToday />
                </Button>
            </Box>
            <Popover
                anchorEl={popupRef.current}
                anchorOrigin={{
                    vertical: "bottom",
                    horizontal: "center",
                }}
                id={isOpen ? "simple-popper" : ""}
                open={isOpen}
                transformOrigin={{
                    vertical: "top",
                    horizontal: "center",
                }}
            >
                <Card
                    className={cardClasses.base}
                    ref={rootRef}
                    style={{ margin: 0 }}
                >
                    <Typography variant="h6">Customize Period</Typography>
                    <ButtonGroup style={{ margin: "16px 0" }}>
                        {Object.keys(ranges).map((key) => (
                            <Button
                                color="primary"
                                key={key}
                                style={{ marginTop: "4px" }}
                                variant={
                                    localDateRange.predefineRangeName === key
                                        ? "contained"
                                        : "outlined"
                                }
                                onClick={(): void => {
                                    const dates = ranges[key];
                                    setDateRange({
                                        from: dates[0],
                                        to: dates[1],
                                        predefineRangeName: key,
                                    });
                                    // Close popup if range isn't custom
                                    if (key !== "Custom") {
                                        setIsOpen(false);
                                    }
                                }}
                            >
                                {key}
                            </Button>
                        ))}
                    </ButtonGroup>
                    <Box display="flex">
                        <ReactDatePicker
                            inline
                            selectsStart
                            showTimeInput
                            calendarClassName={datePickerClasses.datePicker}
                            dateFormat="MM/dd/yyyy h:mm aa"
                            endDate={localDateRange.to}
                            selected={localDateRange.from}
                            startDate={localDateRange.from}
                            onChange={(date: Date): void => {
                                setLocalDateRange({
                                    ...localDateRange,
                                    from: date,
                                    predefineRangeName: "Custom",
                                });
                            }}
                        />
                        <ReactDatePicker
                            inline
                            selectsEnd
                            showTimeInput
                            calendarClassName={datePickerClasses.datePicker}
                            dateFormat="MM/dd/yyyy h:mm aa"
                            endDate={localDateRange.to}
                            minDate={localDateRange.from}
                            selected={localDateRange.to}
                            startDate={localDateRange.from}
                            onChange={(date: Date): void => {
                                setLocalDateRange({
                                    ...localDateRange,
                                    to: date,
                                    predefineRangeName: "Custom",
                                });
                            }}
                        />
                    </Box>
                    <Box marginY={2}>
                        <Button
                            color="primary"
                            variant="contained"
                            onClick={(): void => {
                                setDateRange(localDateRange);
                                setIsOpen(false);
                            }}
                        >
                            {t("label.apply")}
                        </Button>
                        <Button
                            color="primary"
                            style={{ marginLeft: 16 }}
                            variant="outlined"
                            onClick={(): void => {
                                setIsOpen(false);
                                // Reset local date with store, To be in sync
                                setLocalDateRange(dateRange);
                            }}
                        >
                            {t("label.cancel")}
                        </Button>
                    </Box>
                </Card>
            </Popover>
        </div>
    );
};

export default DateRangePicker;
