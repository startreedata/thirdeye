import { Button, Grid, Link } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
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
import { getEventsViewPath } from "../../utils/routes/routes.util";
import { EventListV1Props } from "./event-list-v1.interfaces";

export const EventListV1: FunctionComponent<EventListV1Props> = (
    props: EventListV1Props
) => {
    const { t } = useTranslation();
    const [selectedEvent, setSelectedEvent] =
        useState<DataGridSelectionModelV1<Event>>();
    const navigate = useNavigate();

    const handleEventDelete = (): void => {
        if (!isActionButtonDisable) {
            const selectedUiEvent = props.events?.find(
                (event) => event.id === selectedEvent?.rowKeyValues[0]
            );

            selectedUiEvent &&
                props.onDelete &&
                props.onDelete(selectedUiEvent);
        }
    };

    const handleEventViewDetailsById = (id: number): void =>
        navigate(getEventsViewPath(id));

    const isActionButtonDisable = !(
        selectedEvent && selectedEvent.rowKeyValues.length === 1
    );

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

    const uiEventColumns = [
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
            minWidth: 300,
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
                    columns={uiEventColumns}
                    data={props.events as Event[]}
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.event"),
                    })}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleEventDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedEvent}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
