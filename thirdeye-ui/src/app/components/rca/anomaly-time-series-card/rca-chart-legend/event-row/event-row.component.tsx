import { Button, Checkbox } from "@material-ui/core";
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
                style={{
                    borderLeftWidth: "5px",
                    borderLeftColor: colorScale(event.id),
                    borderLeftStyle: "solid",
                }}
            >
                <Checkbox
                    checked={event.enabled}
                    size="small"
                    onChange={(_, checked) => onCheckBoxClick(event, checked)}
                />
            </TableCell>
            <TableCell>{event.type}</TableCell>
            <TableCell>{event.name}</TableCell>
            <TableCell align="right">
                <LocalThemeProviderV1 primary={lightV1.palette.error}>
                    <Button
                        className={classes.removeBtn}
                        color="primary"
                        size="small"
                        onClick={() => onRemoveBtnClick(event)}
                    >
                        {t("label.remove-event")}
                    </Button>
                </LocalThemeProviderV1>
            </TableCell>
        </TableRow>
    );
};
