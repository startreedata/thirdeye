import { Button, Grid, Link } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { formatDateAndTimeV1 } from "../../platform/utils";
import { Event } from "../../rest/dto/event.interfaces";
import { getSearchDataKeysForEvents } from "../../utils/events/events.util";
import { getEventsViewPath } from "../../utils/routes/routes.util";
import { EventCardV1 } from "../entity-cards/event-card-v1/event-card-v1.component";
import { TimeRangeButtonWithContext } from "../time-range/time-range-button-with-context/time-range-button.component";
import { EventListV1Props } from "./event-list-v1.interfaces";

export const EventListV1: FunctionComponent<EventListV1Props> = ({
    onDelete,
    events,
}) => {
    const { t } = useTranslation();
    const [selectedEvent, setSelectedEvent] =
        useState<DataGridSelectionModelV1<Event>>();
    const navigate = useNavigate();
    const [searchDataKeys, setSearchDataKeys] = useState<string[]>(
        getSearchDataKeysForEvents([])
    );
    const [uiEvents, setUIEvents] = useState<Event[] | null>(null);

    const generateDataWithChildren = (data: Event[]): Event[] => {
        return data?.map((event, index) => ({
            ...event,
            children: [
                {
                    id: index,
                    expandPanelContents: <EventCardV1 event={event} />,
                },
            ],
        }));
    };

    useEffect(() => {
        if (!events) {
            return;
        }

        const newEvents = generateDataWithChildren(events);
        setUIEvents(newEvents);

        setSearchDataKeys(getSearchDataKeysForEvents(events));
    }, [events]);

    const handleEventDelete = (): void => {
        if (!selectedEvent || !selectedEvent.rowKeyValueMap) {
            return;
        }

        onDelete && onDelete(Array.from(selectedEvent.rowKeyValueMap.values()));
    };

    const handleEventViewDetailsById = (id: number): void =>
        navigate(getEventsViewPath(id));

    const nameRenderer = useCallback(
        (_: Record<string, unknown>, data: Event): ReactNode => (
            <Link onClick={() => handleEventViewDetailsById(data.id)}>
                {data.name}
            </Link>
        ),
        []
    );

    const startTimeRenderer = useCallback(
        (_: Record<string, unknown>, data: Event): ReactNode =>
            formatDateAndTimeV1(data.startTime),
        []
    );

    const endTimeRenderer = useCallback(
        (_: Record<string, unknown>, data: Event): ReactNode =>
            formatDateAndTimeV1(data.endTime),
        []
    );

    const eventColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 300,
            sortable: true,
            customCellRenderer: nameRenderer,
        },
        {
            key: "type",
            dataKey: "type",
            header: t("label.type"),
            minWidth: 150,
            sortable: true,
        },
        {
            key: "startTime",
            dataKey: "startTime",
            header: t("label.start"),
            minWidth: 300,
            sortable: true,
            customCellRenderer: startTimeRenderer,
        },
        {
            key: "endTime",
            dataKey: "endTime",
            header: t("label.end"),
            minWidth: 300,
            sortable: true,
            customCellRenderer: endTimeRenderer,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<Event>
                    hideBorder
                    columns={eventColumns}
                    data={uiEvents as Event[]}
                    expandColumnKey="name"
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    searchDataKeys={searchDataKeys}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.event"),
                    })}
                    toolbarComponent={
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                            spacing={2}
                        >
                            <Grid item>
                                <Button
                                    disabled={
                                        !selectedEvent ||
                                        selectedEvent.rowKeyValues.length === 0
                                    }
                                    variant="contained"
                                    onClick={handleEventDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                            <Grid item>
                                <TimeRangeButtonWithContext />
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedEvent}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
