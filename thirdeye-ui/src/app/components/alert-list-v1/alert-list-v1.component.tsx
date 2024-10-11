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
import { Box, Button, Grid, Link } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import {
    DataGridColumnV1,
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { formatDateAndTimeV1 } from "../../platform/utils";
import type { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import {
    getAlertsAlertPath,
    getAlertsCreateCopyPath,
    getAlertsUpdatePath,
} from "../../utils/routes/routes.util";
import { ActiveIndicator } from "../active-indicator/active-indicator.component";
import { AlertAccuracyColored } from "../alert-accuracy-colored/alert-accuracy-colored.component";
import { AlertCardV1 } from "../entity-cards/alert-card-v1/alert-card-v1.component";
import type { AlertListV1Props } from "./alert-list-v1.interfaces";
import { TEST_IDS } from "./alert-list-v1.interfaces";

export const AlertListV1: FunctionComponent<AlertListV1Props> = ({
    alerts,
    onDelete,
    onAlertReset,
}) => {
    const [selectedAlert, setSelectedAlert] =
        useState<DataGridSelectionModelV1<UiAlert>>();
    const [alertsData, setAlertsData] = useState<UiAlert[] | null>(null);
    const navigate = useNavigate();

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
        if (!alerts) {
            return;
        }

        const alertsData = generateDataWithChildren(alerts);
        setAlertsData(alertsData);
    }, [alerts]);

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: UiAlert
    ): ReactElement => {
        return (
            <Link component={RouterLink} to={getAlertsAlertPath(data.id)}>
                {cellValue}
            </Link>
        );
    };

    const renderAlertAccuracy = (
        _: Record<string, unknown>,
        data: UiAlert
    ): ReactElement => (
        <AlertAccuracyColored
            alertId={data.id}
            typographyProps={{ variant: "body2" }}
        />
    );

    const renderAlertStatus = (
        _: Record<string, unknown>,
        data: UiAlert
    ): ReactElement => {
        const active = data.active;

        return <ActiveIndicator active={active} />;
    };

    const renderCreatedString = (
        _: Record<string, unknown>,
        data: UiAlert
    ): ReactElement => {
        if (data.alert?.created) {
            return <>{formatDateAndTimeV1(data.alert?.created)}</>;
        }

        return <></>;
    };

    const isActionButtonDisable = !(
        selectedAlert && selectedAlert.rowKeyValues.length === 1
    );

    const handleAlertDelete = (): void => {
        if (!isActionButtonDisable) {
            const selectedUiAlert = alerts?.find(
                (alert) => alert.id === selectedAlert?.rowKeyValues[0]
            );

            selectedUiAlert && onDelete && onDelete(selectedUiAlert);
        }
    };

    const handleAlertEdit = (): void => {
        if (!selectedAlert) {
            return;
        }
        const selectedAlertId = selectedAlert.rowKeyValues[0] as number;

        navigate(getAlertsUpdatePath(selectedAlertId));
    };

    const handleAlertDuplicate = (): void => {
        if (!selectedAlert) {
            return;
        }
        const selectedAlertId = selectedAlert.rowKeyValues[0] as number;

        navigate(getAlertsCreateCopyPath(selectedAlertId));
    };

    const handleAlertReset = (): void => {
        if (!selectedAlert || !selectedAlert.rowKeyValueMap) {
            return;
        }
        const selectedSingleAlert = selectedAlert.rowKeyValueMap.get(
            selectedAlert.rowKeyValues[0]
        );

        if (!selectedSingleAlert || !selectedSingleAlert.alert) {
            return;
        }

        onAlertReset && onAlertReset(selectedSingleAlert.alert);
    };

    const alertGroupColumns: DataGridColumnV1<UiAlert>[] = [
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
            key: "accuracy",
            dataKey: "accuracy",
            header: t("label.accuracy"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderAlertAccuracy,
            cellTooltip: false,
        },
        {
            key: "active",
            dataKey: "active",
            header: t("label.active"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderAlertStatus,
        },
        {
            key: "created",
            dataKey: "alert.created",
            header: t("label.created"),
            minWidth: 0,
            flex: 1,
            sortable: true,
            customCellRenderer: renderCreatedString,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <Box height="90vh">
                    <DataGridV1<UiAlert>
                        hideBorder
                        columns={alertGroupColumns}
                        data={alertsData as UiAlert[]}
                        data-testId={TEST_IDS.TABLE}
                        expandColumnKey="name"
                        initialSortState={{
                            key: "created",
                            order: DataGridSortOrderV1.DESC,
                        }}
                        rowKey="id"
                        searchPlaceholder={t("label.search-entity", {
                            entity: t("label.alerts"),
                        })}
                        toolbarComponent={
                            <Grid container alignItems="center" spacing={2}>
                                {/* Duplicate */}
                                <Grid item>
                                    <Button
                                        data-testId={TEST_IDS.DUPLICATE_BUTTON}
                                        disabled={isActionButtonDisable}
                                        variant="contained"
                                        onClick={handleAlertDuplicate}
                                    >
                                        {t("label.duplicate")}
                                    </Button>
                                </Grid>

                                {/* Edit */}
                                <Grid item>
                                    <Button
                                        data-testId={TEST_IDS.EDIT_BUTTON}
                                        disabled={isActionButtonDisable}
                                        variant="contained"
                                        onClick={handleAlertEdit}
                                    >
                                        {t("label.edit")}
                                    </Button>
                                </Grid>

                                {/* Delete */}
                                <Grid item>
                                    <Button
                                        data-testId={TEST_IDS.DELETE_BUTTON}
                                        disabled={isActionButtonDisable}
                                        variant="contained"
                                        onClick={handleAlertDelete}
                                    >
                                        {t("label.delete")}
                                    </Button>
                                </Grid>

                                {/* Reset */}
                                <Grid item>
                                    <Button
                                        data-testId={TEST_IDS.RESET_BUTTON}
                                        disabled={isActionButtonDisable}
                                        variant="contained"
                                        onClick={handleAlertReset}
                                    >
                                        {t("label.reset")}
                                    </Button>
                                </Grid>
                            </Grid>
                        }
                        onSelectionChange={setSelectedAlert}
                    />
                </Box>
            </PageContentsCardV1>
        </Grid>
    );
};
