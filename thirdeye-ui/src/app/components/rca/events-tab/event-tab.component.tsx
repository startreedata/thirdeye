import {
    Box,
    CardContent,
    Checkbox,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
} from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { Event } from "../../../rest/dto/event.interfaces";
import { getAllEvents } from "../../../rest/event/events.rest";
import { getErrorMessages } from "../../../utils/rest/rest.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { EventsTabProps } from "./event-tab.interfaces";

export const EventsTab: FunctionComponent<EventsTabProps> = ({
    selectedEvents,
    onCheckClick,
}: EventsTabProps) => {
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const [events, setEvents] = useState<Event[]>();

    const { notify } = useNotificationProviderV1();

    const startTime = searchParams.get(TimeRangeQueryStringKey.START_TIME);
    const endTime = searchParams.get(TimeRangeQueryStringKey.START_TIME);

    useEffect(() => {
        fetchEvents();
    }, [startTime, endTime]);

    const fetchEvents = useCallback(async () => {
        setEvents(undefined);
        let events: Event[] = [];
        try {
            events = await getAllEvents({
                startTime: Number(startTime),
                endTime: Number(endTime),
            });
        } catch (error) {
            const errorMessages = getErrorMessages(error as AxiosError);
            const genericMsg = t("message.error-while-fetching", {
                entity: t("label.events"),
            });
            if (isEmpty(errorMessages)) {
                notify(NotificationTypeV1.Error, genericMsg);
            } else {
                errorMessages.map((msg) =>
                    notify(NotificationTypeV1.Error, `${genericMsg}: ${msg}`)
                );
            }
        } finally {
            onCheckClick([]);
            setEvents(events);
        }
    }, [startTime, endTime, setEvents]);

    const handleOnCheckboxClick = (event: Event, checked: boolean): void => {
        let events: Event[] = [];
        if (checked) {
            events = [...selectedEvents, event];
        } else {
            events = selectedEvents.filter(
                (selectedEvent) => selectedEvent.id !== event.id
            );
        }

        onCheckClick(events);
    };

    return (
        <>
            <CardContent>
                {!events ? (
                    <Box pb={20} pt={20}>
                        <AppLoadingIndicatorV1 />
                    </Box>
                ) : isEmpty(events) ? (
                    <Box pb={20} pt={20}>
                        <NoDataIndicator
                            text={t("message.no-data-for-entity", {
                                entity: t("label.events"),
                            })}
                        />
                    </Box>
                ) : (
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell />
                                <TableCell>
                                    <strong>Name</strong>
                                </TableCell>
                                <TableCell>
                                    <strong>Start time</strong>
                                </TableCell>
                                <TableCell>
                                    <strong>End time</strong>
                                </TableCell>
                                <TableCell>
                                    <strong>Type</strong>
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {events.map((row) => (
                                <TableRow key={row.id}>
                                    <TableCell>
                                        <Checkbox
                                            checked={Boolean(
                                                selectedEvents.find(
                                                    (selectedEvent) =>
                                                        selectedEvent.id ===
                                                        row.id
                                                )
                                            )}
                                            onChange={(_event, checked) =>
                                                handleOnCheckboxClick(
                                                    row,
                                                    checked
                                                )
                                            }
                                        />
                                    </TableCell>
                                    <TableCell component="th" scope="row">
                                        {row.name}
                                    </TableCell>
                                    <TableCell>
                                        {formatDateAndTimeV1(row.startTime)}
                                    </TableCell>
                                    <TableCell>
                                        {formatDateAndTimeV1(row.endTime)}
                                    </TableCell>
                                    <TableCell>{row.type || "-"}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </>
    );
};
