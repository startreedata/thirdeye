/*
 * Copyright 2022 StarTree Inc
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
import { Button, Grid, Link } from "@material-ui/core";
import { capitalize } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    DataGridColumnV1,
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { AnomalyFeedbackType } from "../../rest/dto/anomaly.interfaces";
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

    const handleAlertViewDetails = (id: number): void => {
        navigate(getAlertsAlertPath(id));
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

    const renderAlertAccuracy = (
        _: Record<string, unknown>,
        data: UiAlert
    ): ReactElement => (
        <AlertAccuracyColored
            alertStats={data.accuracyStatistics ?? null}
            renderCustomText={(accuracyNumber) => `${100 * accuracyNumber}%`}
            typographyProps={{ variant: "body2" }}
        />
    );

    const renderAlertAccuracyTooltip = (
        _: Record<string, unknown>,
        data: UiAlert
    ): ReactElement | undefined => {
        if (!data.accuracyStatistics) {
            return (
                <>
                    {capitalize(
                        t("message.fetching-entity", {
                            entity: t("label.data"),
                        })
                    )}
                    ...
                </>
            );
        }

        const { totalCount, countWithFeedback, feedbackStats } =
            data.accuracyStatistics;

        return (
            <>
                {t("message.total-reported-anomalies")}: {totalCount}
                <br />
                {t("message.anomalies-with-feedback")}: {countWithFeedback}
                <br />
                {t("message.misreported-anomalies")}:
                {feedbackStats[AnomalyFeedbackType.NOT_ANOMALY]}
            </>
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
            customCellTooltipRenderer: renderAlertAccuracyTooltip,
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
                            {/* Duplicate */}
                            <Grid item>
                                <Button
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
            </PageContentsCardV1>
        </Grid>
    );
};
