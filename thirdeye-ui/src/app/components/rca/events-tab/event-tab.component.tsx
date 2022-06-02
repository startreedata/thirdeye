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
import Skeleton from "@material-ui/lab/Skeleton";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { Event } from "../../../rest/dto/event.interfaces";
import { useGetEventsForAnomaly } from "../../../rest/event/event.actions";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EventsTabProps } from "./event-tab.interfaces";

export const EventsTab: FunctionComponent<EventsTabProps> = ({
    anomalyId,
    selectedEvents,
    onCheckClick,
}: EventsTabProps) => {
    const { t } = useTranslation();
    const { getEventsForAnomaly, errorMessages, status, events } =
        useGetEventsForAnomaly();

    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        getEventsForAnomaly({
            anomalyId,
        });
    }, [anomalyId]);

    useEffect(() => {
        if (status === ActionStatus.Error) {
            !isEmpty(errorMessages)
                ? errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.dimension-analysis-data"),
                      })
                  );
        }
    }, [status]);

    const handleOnCheckboxClick = (event: Event, checked: boolean): void => {
        let events: Event[];

        if (checked) {
            events = [...selectedEvents, event];
        } else {
            events = selectedEvents.filter(
                (selectedEvent) => selectedEvent.id !== event.id
            );
        }

        onCheckClick(events);
    };

    // Update investigation events with actual event
    useEffect(() => {
        if (selectedEvents && selectedEvents.length && !isEmpty(events)) {
            const copiedSelectedEvents = events?.filter((event) =>
                selectedEvents.find((e) => e.id === event.id)
            );
            onCheckClick(copiedSelectedEvents || []);
        }
    }, [events]);

    return (
        <CardContent>
            {/* Loading Indicator when request is in flight */}
            {status === ActionStatus.Working && (
                <>
                    <Skeleton height={50} variant="text" />
                    <Skeleton height={50} variant="text" />
                    <Skeleton height={50} variant="text" />
                    <Skeleton height={50} variant="text" />
                </>
            )}

            {status === ActionStatus.Done && events && events.length > 0 && (
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
                                                    selectedEvent.id === row.id
                                            )
                                        )}
                                        onChange={(_event, checked) =>
                                            handleOnCheckboxClick(row, checked)
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
            {status === ActionStatus.Done && events && events.length === 0 && (
                <Box pb={20} pt={20}>
                    <NoDataIndicator
                        text={t("message.no-data-for-entity", {
                            entity: t("label.events"),
                        })}
                    />
                </Box>
            )}
        </CardContent>
    );
};
