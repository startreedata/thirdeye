import {
    Button,
    Grid,
    Link,
    Paper,
    useMediaQuery,
    useTheme,
} from "@material-ui/core";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import {
    AppLoadingIndicatorV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAlertsViewPath } from "../../utils/routes/routes.util";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelectorV1 } from "../time-range/time-range-selector/time-range-selector-v1/time-range-selector-v1.component";
import { AlertListProps } from "./alert-list.interfaces";
import { useAlertListStyles } from "./alert-list.styles";

export const AlertList: FunctionComponent<AlertListProps> = (
    props: AlertListProps
) => {
    const [
        selectedAlert,
        setSelectedAlert,
    ] = useState<DataGridSelectionModelV1>();
    const history = useHistory();

    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
    } = useTimeRange();

    const { t } = useTranslation();
    const theme = useTheme();
    const { timeRangeContainer } = useAlertListStyles();

    const handleAlertViewDetails = (id: number): void => {
        history.push(getAlertsViewPath(id));
    };

    const renderLink = ({ rowData }: { rowData: UiAlert }): ReactElement => {
        return (
            <Link onClick={() => handleAlertViewDetails(rowData.id)}>
                {rowData.name}
            </Link>
        );
    };

    const renderAlertStatus = ({
        rowData,
    }: {
        rowData: UiAlert;
    }): ReactElement => {
        const { active } = rowData;

        return (
            <>
                {/* Active */}
                {active && (
                    <CheckIcon
                        fontSize="small"
                        htmlColor={theme.palette.success.main}
                    />
                )}

                {/* Inactive */}
                {!active && (
                    <CloseIcon
                        fontSize="small"
                        htmlColor={theme.palette.error.main}
                    />
                )}
            </>
        );
    };

    const isActionButtonDisable = !(
        selectedAlert && selectedAlert.rowKeyValues.length === 1
    );

    const handleAlertDelete = (): void => {
        if (!isActionButtonDisable) {
            const selectedUiAlert = props.alerts?.find(
                (alert) => alert.id === selectedAlert?.rowKeyValues[0]
            );
            selectedUiAlert &&
                props.onDelete &&
                props.onDelete(selectedUiAlert);
        }
    };

    const alertGroupColumns = [
        {
            key: "name",
            dataKey: "name",
            title: t("label.alert-name"),
            width: 0,
            flexGrow: 1.5,
            sortable: true,
            cellRenderer: renderLink,
        },
        {
            key: "createdBy",
            dataKey: "createdBy",
            title: t("label.created-by"),
            width: 0,
            flexGrow: 1,
        },
        {
            key: "active",
            dataKey: "active",
            title: t("label.active"),
            width: 0,
            flexGrow: 1,
            cellRenderer: renderAlertStatus,
        },
    ];

    const screenWidthSmUp = useMediaQuery(theme.breakpoints.up("sm"));

    return props.alerts ? (
        <Grid item xs={12}>
            <Paper className={timeRangeContainer} elevation={0}>
                <PageContentsCardV1>
                    <TimeRangeSelectorV1
                        hideTimeRange={!screenWidthSmUp}
                        recentCustomTimeRangeDurations={
                            recentCustomTimeRangeDurations
                        }
                        timeRangeDuration={timeRangeDuration}
                        onChange={setTimeRangeDuration}
                    />
                </PageContentsCardV1>
            </Paper>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1
                    disableBorder
                    columns={alertGroupColumns}
                    data={props.alerts}
                    rowKey="id"
                    selection={selectedAlert}
                    toolbarComponent={
                        <Grid>
                            <Button
                                disabled={isActionButtonDisable}
                                variant="contained"
                                onClick={handleAlertDelete}
                            >
                                {t("label.delete")}
                            </Button>
                        </Grid>
                    }
                    onSelectionChange={setSelectedAlert}
                />
            </PageContentsCardV1>
        </Grid>
    ) : (
        <AppLoadingIndicatorV1 />
    );
};
