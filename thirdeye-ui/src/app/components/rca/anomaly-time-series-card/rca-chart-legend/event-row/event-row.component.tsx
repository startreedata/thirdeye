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
import { Button, Checkbox, Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import { lightV1, LocalThemeProviderV1 } from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useFilteredTimeSeriesRowStyles } from "../filtered-time-series-row/filtered-time-series-row.styles";
import { EventRowProps } from "./event-row.interfaces";

export const EventRow: FunctionComponent<EventRowProps> = ({
    event,
    colorScale,
    onRemoveBtnClick,
    onCheckBoxClick,
}) => {
    const classes = useFilteredTimeSeriesRowStyles();
    const { t } = useTranslation();

    return (
        <TableRow>
            <TableCell
                className={classes.assignedChartColorCell}
                style={{
                    borderLeftColor: colorScale(event.id),
                }}
            >
                <Checkbox
                    checked={event.enabled}
                    size="small"
                    onChange={(_, checked) => onCheckBoxClick(event, checked)}
                />
            </TableCell>
            <TableCell>
                <Typography variant="body2">{t("label.event")}</Typography>
            </TableCell>
            <TableCell>
                <Typography variant="body2">
                    {event.type}/{event.name}
                </Typography>
            </TableCell>
            <TableCell align="right">
                <LocalThemeProviderV1 primary={lightV1.palette.error}>
                    <Button
                        className={classes.removeBtn}
                        color="primary"
                        size="small"
                        onClick={() => onRemoveBtnClick(event)}
                    >
                        {t("label.remove")}
                    </Button>
                </LocalThemeProviderV1>
            </TableCell>
        </TableRow>
    );
};
