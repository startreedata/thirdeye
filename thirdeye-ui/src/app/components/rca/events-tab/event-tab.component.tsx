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
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AppLoadingIndicatorV1 } from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { Event } from "../../../rest/dto/event.interfaces";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EventsTabProps } from "./event-tab.interfaces";

export const EventsTab: FunctionComponent<EventsTabProps> = ({
    selectedEvents,
    onCheckClick,
    events,
}: EventsTabProps) => {
    const { t } = useTranslation();
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
                            text={t(
                                "message.no-data-for-entity-for-date-range",
                                { entity: t("label.events") }
                            )}
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
