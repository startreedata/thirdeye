import { Box, CardContent } from "@material-ui/core";
import { isEmpty, map } from "lodash";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    NotificationTypeV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { Event } from "../../../rest/dto/event.interfaces";
import { useGetEventsForAnomaly } from "../../../rest/event/event.actions";
import {
    getSearchDataKeysForEvents,
    handleEventsSearch,
} from "../../../utils/events/events.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EventsTabProps } from "./event-tab.interfaces";

export const EventsTab: FunctionComponent<EventsTabProps> = ({
    anomalyId,
    selectedEvents,
    onCheckClick,
    searchValue,
}: EventsTabProps) => {
    const { t } = useTranslation();
    const { getEventsForAnomaly, errorMessages, status, events } =
        useGetEventsForAnomaly();

    const { notify } = useNotificationProviderV1();

    const [searchDataKeys, setSearchDataKeys] = useState<string[]>(
        getSearchDataKeysForEvents([])
    );
    const [filteredEvents, setFilteredEvents] = useState<Event[]>([]);

    // SelectionModel to show selection on data-grid
    const selectionModel: DataGridSelectionModelV1<Event> = useMemo(
        () => ({
            rowKeyValues: selectedEvents.map((e: Event) => e.id),
            rowKeyValueMap: new Map(
                selectedEvents.map((event) => [event.id, event])
            ),
        }),
        [selectedEvents]
    );

    useEffect(() => {
        setFilteredEvents(
            handleEventsSearch(searchValue, events, searchDataKeys)
        );
    }, [searchValue, events, searchDataKeys]);

    const onSelectionChange = (
        selectedEvent: DataGridSelectionModelV1<Event>
    ): void => {
        onCheckClick(Array.from(selectedEvent?.rowKeyValueMap?.values() || []));
    };

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

    // Update event, searchDataKey and selectedEvents data
    useEffect(() => {
        if (!events) {
            return;
        }

        setSearchDataKeys(getSearchDataKeysForEvents(events));

        if (selectedEvents && selectedEvents.length) {
            const copiedSelectedEvents = events?.filter((event) =>
                selectedEvents.find((e) => e.id === event.id)
            );
            onCheckClick(copiedSelectedEvents || []);
        }
    }, [events]);

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

    const metadataRenderer = useCallback(
        (_: Record<string, unknown>, data: Event): ReactNode => (
            <Box marginY={0.5}>
                {map(
                    data.targetDimensionMap,
                    (value: string[], key: string) => (
                        <div key={key}>
                            {key}: {value.join(", ")}
                        </div>
                    )
                )}
            </Box>
        ),
        []
    );

    const eventColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 300,
        },
        {
            key: "type",
            dataKey: "type",
            header: t("label.type"),
            minWidth: 180,
        },
        {
            key: "startTime",
            dataKey: "startTime",
            header: t("label.start"),
            minWidth: 300,
            customCellRenderer: startTimeRenderer,
        },
        {
            key: "endTime",
            dataKey: "endTime",
            header: t("label.end"),
            minWidth: 300,
            customCellRenderer: endTimeRenderer,
        },
        {
            key: "targetDimensionMap",
            dataKey: "targetDimensionMap",
            header: t("label.metadata"),
            minWidth: 300,
            customCellRenderer: metadataRenderer,
        },
    ];

    return (
        <CardContent>
            {/* Loading Indicator when request is in flight */}
            {status === ActionStatus.Working && (
                <SkeletonV1 preventDelay height={200} variant="rect" />
            )}

            {status === ActionStatus.Done && events && events.length > 0 && (
                <DataGridV1<Event>
                    hideBorder
                    hideToolbar
                    columns={eventColumns}
                    data={filteredEvents as Event[]}
                    rowKey="id"
                    scroll={DataGridScrollV1.Body}
                    searchDataKeys={searchDataKeys}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.event"),
                    })}
                    selectionModel={selectionModel}
                    onSelectionChange={onSelectionChange}
                />
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
