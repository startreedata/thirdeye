import {
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
} from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { getEventsViewPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { EventCardProps } from "./event-card.interfaces";

export const EventCard: FunctionComponent<EventCardProps> = (
    props: EventCardProps
) => {
    const [eventOptionsAnchorElement, setEventOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleEventOptionsClick = (event: MouseEvent<HTMLElement>): void =>
        setEventOptionsAnchorElement(event.currentTarget);

    const handleEventOptionsClose = (): void =>
        setEventOptionsAnchorElement(null);

    const handleEventViewDetails = (): void => {
        if (!props.event) {
            return;
        }

        navigate(getEventsViewPath(props.event.id));
        handleEventOptionsClose();
    };

    const handleEventDelete = (): void => {
        if (!props.event) {
            return;
        }

        props.onDelete && props.onDelete(props.event);
        handleEventOptionsClose();
    };

    return (
        <Card variant="outlined">
            {props.event && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={0}>
                            <Grid item>
                                {/* Event options button */}
                                <IconButton
                                    color="secondary"
                                    onClick={handleEventOptionsClick}
                                >
                                    <MoreVertIcon />
                                </IconButton>

                                {/* Event options */}
                                <Menu
                                    anchorEl={eventOptionsAnchorElement}
                                    open={Boolean(eventOptionsAnchorElement)}
                                    onClose={handleEventOptionsClose}
                                >
                                    {/* View details */}
                                    {props.showViewDetails && (
                                        <MenuItem
                                            onClick={handleEventViewDetails}
                                        >
                                            {t("label.view-details")}
                                        </MenuItem>
                                    )}

                                    {/* Delete event */}
                                    <MenuItem onClick={handleEventDelete}>
                                        {t("label.delete-entity", {
                                            entity: t("label.event"),
                                        })}
                                    </MenuItem>
                                </Menu>
                            </Grid>
                        </Grid>
                    }
                    title={
                        <>
                            {/* Event name */}
                            {props.showViewDetails && (
                                <Link onClick={handleEventViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.event.name}
                                    />
                                </Link>
                            )}

                            {/* Summary */}
                            {!props.showViewDetails && t("label.summary")}
                        </>
                    }
                    titleTypographyProps={{ variant: "h6" }}
                />
            )}

            <CardContent>
                {props.event && (
                    <Grid container>
                        {/* Event */}
                        <Grid item md={4} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.event")}
                                searchWords={props.searchWords}
                                values={[props.event.name]}
                            />
                        </Grid>

                        <Grid item md={4} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.start")}
                                searchWords={props.searchWords}
                                values={[
                                    formatDateAndTimeV1(props.event.startTime),
                                ]}
                            />
                        </Grid>
                        <Grid item md={4} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.end")}
                                searchWords={props.searchWords}
                                values={[
                                    formatDateAndTimeV1(props.event.endTime),
                                ]}
                            />
                        </Grid>

                        <Grid item md={4} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.type")}
                                searchWords={props.searchWords}
                                values={[
                                    props.event.type ||
                                        t("label.no-data-marker"),
                                ]}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.event && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
