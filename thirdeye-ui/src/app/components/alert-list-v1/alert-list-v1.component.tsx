import { Button, Grid, Link } from "@material-ui/core";
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
import {
    getAlertsUpdatePath,
    getAlertsViewPath,
} from "../../utils/routes/routes.util";
import { ActiveIndicator } from "../active-indicator/active-indicator.component";
import { AlertCardV1 } from "../entity-cards/alert-card-v1/alert-card-v1.component";
import { AlertListV1Props } from "./alert-list-v1.interfaces";

export const AlertListV1: FunctionComponent<AlertListV1Props> = (
    props: AlertListV1Props
) => {
    const [selectedAlert, setSelectedAlert] = useState<
        DataGridSelectionModelV1<UiAlert>
    >();
    const [alertsData, setAlertsData] = useState<UiAlert[] | null>(null);
    const history = useHistory();

    const { t } = useTranslation();

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
        data: UiAlert
    ): ReactElement => {
        return (
            <Link onClick={() => handleAlertViewDetails(data.id)}>
                {cellValue}
            </Link>
        );
    };

    const renderAlertStatus = (
        _: Record<string, unknown>,
        data: UiAlert
    ): ReactElement => {
        const active = data.active;

        return <ActiveIndicator active={active} />;
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

    const handleAlertEdit = (): void => {
        if (!selectedAlert) {
            return;
        }
        const selectedAlertId = selectedAlert.rowKeyValues[0] as number;

        history.push(getAlertsUpdatePath(selectedAlertId));
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
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<UiAlert>
                    hideBorder
                    columns={alertGroupColumns}
                    data={alertsData as UiAlert[]}
                    expandColumnKey="name"
                    rowKey="id"
                    scroll={DataGridScrollV1.Body}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.alerts"),
                    })}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            {/* Edit */}
                            <Grid item>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleAlertEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            {/* Delete */}
                            <Grid>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleAlertDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedAlert}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
