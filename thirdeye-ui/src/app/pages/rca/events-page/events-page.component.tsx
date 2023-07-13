/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Button, Divider, Grid, Typography } from "@material-ui/core";
import { AxiosError } from "axios";
import { clone, isEmpty, map } from "lodash";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { EventsWizardModal } from "../../../components/events-wizard-modal/event-wizard-modal.component";
import { EmptyStateSwitch } from "../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { InvestigationPreview } from "../../../components/rca/investigation-preview/investigation-preview.component";
import { PreviewChart } from "../../../components/rca/top-contributors-table/preview-chart/preview-chart.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    NotificationTypeV1,
    PageContentsCardV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { EditableEvent, Event } from "../../../rest/dto/event.interfaces";
import {
    Investigation,
    SavedStateKeys,
} from "../../../rest/dto/rca.interfaces";
import { useGetEventsForAnomaly } from "../../../rest/event/event.actions";
import { createEvent } from "../../../rest/event/events.rest";
import { determineTimezoneFromAlertInEvaluation } from "../../../utils/alerts/alerts.util";
import { createEmptyEvent } from "../../../utils/events/events.util";
import { getFromSavedInvestigationOrDefault } from "../../../utils/investigation/investigation.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../../utils/rest/rest.util";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { InvestigationContext } from "../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";

export const EventsPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const { notify } = useNotificationProviderV1();
    const { getEventsForAnomaly, errorMessages, status, events } =
        useGetEventsForAnomaly();

    const { investigation, anomaly, alertInsight, onInvestigationChange } =
        useOutletContext<InvestigationContext>();

    const [selectedEvents, setSelectedEvents] = useState<Event[]>(
        getFromSavedInvestigationOrDefault<Event[]>(
            investigation,
            SavedStateKeys.EVENT_SET,
            []
        )
    );

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

    const onSelectionChange = (
        selectedEvent: DataGridSelectionModelV1<Event>
    ): void => {
        setSelectedEvents(
            Array.from(selectedEvent?.rowKeyValueMap?.values() || [])
        );
    };

    const newEventObject = useMemo(() => {
        const event = createEmptyEvent();
        event.startTime = anomaly?.startTime as number;
        event.endTime = anomaly?.endTime as number;

        return event;
    }, [anomaly]);

    const fetchEvents = (): void => {
        anomaly &&
            getEventsForAnomaly({
                anomalyId: anomaly.id,
            });
    };

    useEffect(() => {
        fetchEvents();
    }, [anomaly]);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.dimension-analysis-data"),
            })
        );
    }, [status]);

    const startTimeRenderer = useCallback(
        (_: Record<string, unknown>, data: Event): ReactNode =>
            formatDateAndTimeV1(
                data.startTime,
                determineTimezoneFromAlertInEvaluation(
                    alertInsight?.templateWithProperties
                )
            ),
        [alertInsight]
    );

    const endTimeRenderer = useCallback(
        (_: Record<string, unknown>, data: Event): ReactNode =>
            formatDateAndTimeV1(
                data.endTime,
                determineTimezoneFromAlertInEvaluation(
                    alertInsight?.templateWithProperties
                )
            ),
        [alertInsight]
    );

    const eventColumns = useMemo(() => {
        return [
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
                customCellRenderer: (
                    _: Record<string, unknown>,
                    data: Event
                ): ReactNode => (
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
            },
        ];
    }, [startTimeRenderer, endTimeRenderer]);

    const handleAddEventSubmit = (newEvent: EditableEvent): void => {
        createEvent(newEvent)
            .then((): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.event"),
                    })
                );
                fetchEvents();
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.create-error", {
                        entity: t("label.event"),
                    })
                );
            });
    };

    const handleAddEventsToInvestigationClick = (): void => {
        const copied: Investigation = { ...investigation };
        copied.uiMetadata[SavedStateKeys.EVENT_SET] = clone(selectedEvents);
        onInvestigationChange(copied);
    };

    return (
        <>
            <Grid item xs={12}>
                <Typography variant="h4">
                    {t("label.an-event-could-have-caused-it")}
                </Typography>
                <Typography variant="body1">
                    {t("message.events-allow-you-to-mark-special-dates-in-the")}
                </Typography>
            </Grid>

            <Grid item xs={12}>
                <PageContentsCardV1>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="space-between"
                    >
                        <Grid item xs>
                            {t(
                                "message.select-the-events-to-see-the-dates-plotted-in-the"
                            )}
                        </Grid>
                        <Grid item xs>
                            <Grid container justifyContent="flex-end">
                                <Grid item>
                                    <Divider orientation="vertical" />
                                </Grid>
                                <Grid item>
                                    {t("label.cant-find-an-event")}
                                </Grid>
                                <Grid item>
                                    <EventsWizardModal
                                        btnSize="small"
                                        event={newEventObject}
                                        onSubmit={handleAddEventSubmit}
                                    />
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                    <Box pb={2} pt={2}>
                        <LoadingErrorStateSwitch
                            isError={status === ActionStatus.Error}
                            isLoading={
                                status === ActionStatus.Working ||
                                status === ActionStatus.Initial
                            }
                            loadingState={
                                <>
                                    <Box pb={2} pt={2}>
                                        <SkeletonV1
                                            animation="pulse"
                                            height={300}
                                            variant="rect"
                                        />
                                    </Box>
                                    <Box pb={2} pt={2}>
                                        <SkeletonV1
                                            animation="pulse"
                                            height={300}
                                            variant="rect"
                                        />
                                    </Box>
                                </>
                            }
                        >
                            <EmptyStateSwitch
                                emptyState={
                                    <Box p={2} textAlign="center" width="100%">
                                        {t(
                                            "message.no-events-available-for-anomaly-time-period"
                                        )}
                                    </Box>
                                }
                                isEmpty={isEmpty(events)}
                            >
                                <DataGridV1<Event>
                                    hideBorder
                                    hideToolbar
                                    columns={eventColumns}
                                    data={events as Event[]}
                                    rowKey="id"
                                    scroll={DataGridScrollV1.Body}
                                    searchPlaceholder={t(
                                        "label.search-entity",
                                        {
                                            entity: t("label.event"),
                                        }
                                    )}
                                    selectionModel={selectionModel}
                                    onSelectionChange={onSelectionChange}
                                />

                                <Box pt={2}>
                                    <PreviewChart
                                        alertInsight={alertInsight}
                                        anomaly={anomaly}
                                        dimensionCombinations={[]}
                                        events={selectedEvents}
                                    >
                                        <Button
                                            color="primary"
                                            disabled={isEmpty([selectedEvents])}
                                            onClick={
                                                handleAddEventsToInvestigationClick
                                            }
                                        >
                                            {t(
                                                "label.add-events-to-investigation"
                                            )}
                                        </Button>
                                    </PreviewChart>
                                </Box>
                            </EmptyStateSwitch>
                        </LoadingErrorStateSwitch>
                    </Box>
                </PageContentsCardV1>
            </Grid>

            <Grid item xs={12}>
                <InvestigationPreview
                    alertInsight={alertInsight}
                    anomaly={anomaly}
                    investigation={investigation}
                    onInvestigationChange={onInvestigationChange}
                />
            </Grid>

            <WizardBottomBar
                backBtnLink={`../${
                    AppRouteRelative.RCA_WHAT_WHERE
                }?${searchParams.toString()}`}
                nextBtnLink={`../${
                    AppRouteRelative.RCA_REVIEW_SHARE
                }?${searchParams.toString()}`}
            />
        </>
    );
};
