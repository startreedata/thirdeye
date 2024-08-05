import React, { ReactElement } from "react";
import { AlertEffectivenessProps } from "./alert-effectiveness.interfaces";
import { Link, Typography } from "@material-ui/core";
import { Link as RouterLink } from "react-router-dom";
import { useStyles } from "./alert-effectiveness.styles";
import {
    TableColumns,
    TableRow,
} from "../../../platform/components/table/table.interfaces";
import DataTable from "../../../platform/components/table/table.component";
import { getAlertsAlertPath } from "../../../utils/routes/routes.util";
import AnalysisPeriod from "../anaylysis-period/analysis-period.component";
/*
 * Copyright 2024 StarTree Inc
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
const AlertEffectiveness = ({
    mostActiveAlerts,
    analysisPeriods,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
}: AlertEffectivenessProps): ReactElement => {
    const componentStyles = useStyles();
    const renderLink = (data: TableRow): ReactElement => {
        const id = Number(data.id);

        return (
            <Link component={RouterLink} to={getAlertsAlertPath(id)}>
                {data.name}
            </Link>
        );
    };
    const columns: TableColumns[] = [
        { title: "Alert name", datakey: "name", customRender: renderLink },
        { title: "Anomalies detected", datakey: "anomaliesCount" },
        { title: "Dimensions within alert", datakey: "dimensionsCount" },
    ];

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <Typography>Alerts effectiveness</Typography>
                <AnalysisPeriod
                    analysisPeriods={analysisPeriods}
                    selectedPeriod={selectedAnalysisPeriod}
                    onClick={onAnalysisPeriodChange}
                />
            </div>
            <div>
                <div className={componentStyles.heading}>
                    <Typography>Most active alerts</Typography>
                </div>
                <div className={componentStyles.table}>
                    <DataTable columns={columns} data={mostActiveAlerts} />
                </div>
            </div>
        </>
    );
};

export default AlertEffectiveness;
