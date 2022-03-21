import { Button, Grid, Link, Paper, useTheme } from "@material-ui/core";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "@startree-ui/platform-ui";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAlertsViewPath } from "../../utils/routes/routes.util";
import { AlertCardV1 } from "../entity-cards/alert-card-v1/alert-card-v1.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelectorV1 } from "../time-range/time-range-selector/time-range-selector-v1/time-range-selector-v1.component";
import { AlertListV1Props } from "./alert-list-v1.interfaces";
import { useAlertListV1Styles } from "./alert-list-v1.styles";

export const AlertListV1: FunctionComponent<AlertListV1Props> = (
    props: AlertListV1Props
) => {
    const [
        selectedAlert,
        setSelectedAlert,
    ] = useState<DataGridSelectionModelV1>();
    const [alertsData, setAlertsData] = useState<UiAlert[] | null>(null);
    const history = useHistory();

    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
    } = useTimeRange();

    const { t } = useTranslation();
    const theme = useTheme();
    const { timeRangeContainer } = useAlertListV1Styles();

    const generateDataWithChildren = (data: UiAlert[]): UiAlert[] => {
        return data?.map((alert, index) => ({
            ...alert,
            children: [
                {
                    id: index,
                    expandPanelContents: <AlertCardV1 uiAlert={alert} />,
                },
            ],
        }));
    };

    useEffect(() => {
        if (!props.alerts) {
            return;
        }

        const alertsData = generateDataWithChildren(props.alerts);
        setAlertsData(alertsData);
    }, [props.alerts]);

    const handleAlertViewDetails = (id: number): void => {
        history.push(getAlertsViewPath(id));
    };

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: Record<string, unknown>
    ): ReactElement => {
        return (
            <Link
                onClick={() =>
                    handleAlertViewDetails(((data as unknown) as UiAlert).id)
                }
            >
                {cellValue}
            </Link>
        );
    };

    const renderAlertStatus = (
        _: Record<string, unknown>,
        data: Record<string, unknown>
    ): ReactElement => {
        const active = ((data as unknown) as UiAlert).active;

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
            header: t("label.alert-name"),
            minWidth: 0,
            flex: 1.5,
            sortable: true,
            customCellRenderer: renderLink,
        },
        {
            key: "createdBy",
            dataKey: "createdBy",
            header: t("label.created-by"),
            minWidth: 0,
            flex: 1,
        },
        {
            key: "active",
            dataKey: "active",
            header: t("label.active"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderAlertStatus,
        },
    ];

    return (
        <Grid item xs={12}>
            <Paper className={timeRangeContainer} elevation={0}>
                <PageContentsCardV1>
                    <TimeRangeSelectorV1
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
                    hideBorder
                    columns={alertGroupColumns}
                    data={(alertsData as unknown) as Record<string, unknown>[]}
                    expandColumnKey="name"
                    rowKey="id"
                    scroll={DataGridScrollV1.Body}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.alerts"),
                    })}
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
    );
};
