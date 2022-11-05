import { Button, Checkbox, Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { LocalThemeProviderV1 } from "../../../../../platform/components";
import { lightV1 } from "../../../../../platform/utils";
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
