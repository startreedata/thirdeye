/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Typography } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import InfoIcon from "@material-ui/icons/Info";
import Alert from "@material-ui/lab/Alert";
import { scaleOrdinal } from "@visx/scale";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Event } from "../../../../rest/dto/event.interfaces";
import { Legend } from "../../../visualizations/time-series-chart/legend/legend.component";
import { EventWithChartState } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { COLOR_PALETTE } from "../../../visualizations/time-series-chart/time-series-chart.utils";
import { EventRow } from "./event-row/event-row.component";
import { FilteredTimeSeriesRow } from "./filtered-time-series-row/filtered-time-series-row.component";
import { RCAChartLegendProps } from "./rca-chart-legend.interfaces";
import { useRCACHartLegendStyles } from "./rca-chart-legend.styles";

const OTHER_TIME_SERIES_IDX_OFFSET = 3;

export const RCAChartLegend: FunctionComponent<RCAChartLegendProps> = ({
    series,
    onSeriesClick,
    colorScale,
    timeSeriesFiltersSet,
    events,
    onRemoveBtnClick,
    onEventSelectionChange,
    onEventsStateChange,
}) => {
    const classes = useRCACHartLegendStyles();
    const { t } = useTranslation();

    const currentBaselineBoundsSeries = series.slice(0, 3);
    const currentBaselineBoundsColorScale = scaleOrdinal({
        domain: currentBaselineBoundsSeries.map((x) => x.name) as string[],
        range: currentBaselineBoundsSeries.map((x) =>
            colorScale(x.name as string)
        ),
    });

    const eventsColorScale = scaleOrdinal({
        domain: events.map((x) => x.id) as number[],
        range: COLOR_PALETTE,
    });

    const handleEventRemove = (event: Event): void => {
        onEventSelectionChange(
            events.filter((existingEvent) => existingEvent.id !== event.id)
        );
    };

    const handleEventCheckClick = (
        event: EventWithChartState,
        newCheckedState: boolean
    ): void => {
        event.enabled = newCheckedState;
        onEventsStateChange([...events]);
    };

    return (
        <>
            <Legend
                colorScale={currentBaselineBoundsColorScale}
                events={events}
                series={currentBaselineBoundsSeries}
                onEventsStateChange={onEventsStateChange}
                onSeriesClick={onSeriesClick}
            />
            {(timeSeriesFiltersSet.length > 0 || events.length > 0) && (
                <>
                    <Box padding="30px">
                        <Alert
                            className={classes.infoAlert}
                            icon={<InfoIcon />}
                            severity="info"
                        >
                            {t("message.rca-legend-information")}
                        </Alert>
                    </Box>
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell width="40px" />
                                <TableCell width="100px">
                                    <Typography variant="body2">
                                        <strong>Type</strong>
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2">
                                        <strong>
                                            {t("label.additional-chart-items")}
                                        </strong>
                                    </Typography>
                                </TableCell>
                                <TableCell />
                            </TableRow>
                        </TableHead>
                        {timeSeriesFiltersSet.length > 0 && (
                            <TableBody>
                                {timeSeriesFiltersSet.map((filterSet, idx) => {
                                    return (
                                        <FilteredTimeSeriesRow
                                            colorScale={colorScale}
                                            filterSet={filterSet}
                                            key={idx}
                                            series={series}
                                            onCheckBoxClick={() => {
                                                onSeriesClick &&
                                                    onSeriesClick(
                                                        idx +
                                                            OTHER_TIME_SERIES_IDX_OFFSET
                                                    );
                                            }}
                                            onRemoveBtnClick={() =>
                                                onRemoveBtnClick(idx)
                                            }
                                        />
                                    );
                                })}
                            </TableBody>
                        )}
                        {events.length > 0 && (
                            <TableBody>
                                {events.map((event, idx) => {
                                    return (
                                        <EventRow
                                            colorScale={eventsColorScale}
                                            event={event}
                                            key={`event-${idx}`}
                                            onCheckBoxClick={
                                                handleEventCheckClick
                                            }
                                            onRemoveBtnClick={handleEventRemove}
                                        />
                                    );
                                })}
                            </TableBody>
                        )}
                    </Table>
                </>
            )}
        </>
    );
};
