import { Button, Typography } from "@material-ui/core";
import {
    DataGridSelectionModelV1,
    DataGridV1,
    linkRendererV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, ReactNode, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    getAlertsViewPath,
    getAnomaliesViewIndexPath,
} from "../../utils/routes/routes.util";
import { AnomalyListV1Props } from "./anomaly-list-v1.interfaces";

export const AnomalyListV1: FunctionComponent<AnomalyListV1Props> = (
    props: AnomalyListV1Props
) => {
    const [selectedAnomaly, setSelectedAnomaly] = useState<
        DataGridSelectionModelV1<UiAnomaly>
    >();
    const { t } = useTranslation();

    const anomalyNameRenderer = (
        cellValue: Record<string, unknown>,
        data: UiAnomaly
    ): ReactNode => {
        return linkRendererV1(cellValue, getAnomaliesViewIndexPath(data.id));
    };

    const alertNameRenderer = (
        cellValue: Record<string, unknown>,
        data: UiAnomaly
    ): ReactNode => {
        return linkRendererV1(cellValue, getAlertsViewPath(data.alertId));
    };

    const deviationRenderer = (
        cellValue: Record<string, unknown>,
        data: UiAnomaly
    ): ReactNode => {
        return (
            <Typography
                color={data.negativeDeviation ? "error" : undefined}
                variant="body2"
            >
                {cellValue}
            </Typography>
        );
    };

    const isActionButtonDisable = !(
        selectedAnomaly && selectedAnomaly.rowKeyValues.length === 1
    );

    const handleAnomalyDelete = (): void => {
        if (!selectedAnomaly) {
            return;
        }

        const anomalyId = selectedAnomaly.rowKeyValues[0];
        const anomaly = selectedAnomaly.rowKeyValueMap?.get(anomalyId);

        props.onDelete && props.onDelete(anomaly as UiAnomaly);
    };

    const anomalyListColumns = useMemo(
        () => [
            {
                key: "name",
                dataKey: "name",
                header: t("label.name"),
                sortable: true,
                minWidth: 200,
                customCellRenderer: anomalyNameRenderer,
            },
            {
                key: "alertName",
                dataKey: "alertName",
                header: t("label.alert"),
                sortable: true,
                minWidth: 300,
                customCellRenderer: alertNameRenderer,
            },
            {
                key: "duration",
                dataKey: "duration",
                header: t("label.duration"),
                sortable: true,
                minWidth: 150,
            },
            {
                key: "startTime",
                dataKey: "startTime",
                header: t("label.start"),
                sortable: true,
                minWidth: 200,
            },
            {
                key: "endTime",
                dataKey: "endTime",
                header: t("label.end"),
                sortable: true,
                minWidth: 200,
            },
            {
                key: "current",
                dataKey: "current",
                header: t("label.current"),
                sortable: true,
                minWidth: 150,
            },
            {
                key: "predicted",
                dataKey: "predicted",
                header: t("label.predicted"),
                sortable: true,
                minWidth: 150,
            },
            {
                key: "deviation",
                dataKey: "deviation",
                header: t("label.deviation"),
                sortable: true,
                minWidth: 150,
                customCellRenderer: deviationRenderer,
            },
        ],
        [anomalyNameRenderer, alertNameRenderer, deviationRenderer]
    );

    return (
        <DataGridV1<UiAnomaly>
            hideBorder
            columns={anomalyListColumns}
            data={props.anomalies as UiAnomaly[]}
            rowKey="id"
            searchPlaceholder={t("label.search-entity", {
                entity: t("label.anomalies"),
            })}
            toolbarComponent={
                <Button
                    disabled={isActionButtonDisable}
                    variant="contained"
                    onClick={handleAnomalyDelete}
                >
                    {t("label.delete")}
                </Button>
            }
            onSelectionChange={setSelectedAnomaly}
        />
    );
};
