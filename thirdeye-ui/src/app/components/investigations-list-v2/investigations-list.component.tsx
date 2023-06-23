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
import { Grid, Link } from "@material-ui/core";
import OpenInNewIcon from "@material-ui/icons/OpenInNew";
import React, { FunctionComponent, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridScrollV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { formatDateAndTimeV1 } from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import { INVESTIGATION_ID_QUERY_PARAM } from "../../utils/investigation/investigation.util";
import { getRootCauseAnalysisForAnomalyInvestigateV2Path } from "../../utils/routes/routes.util";
import { EmptyStateSwitch } from "../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { InvestigationsListProps } from "./investigations-list.interfaces";

export const InvestigationsList: FunctionComponent<InvestigationsListProps> = ({
    investigations,
    getInvestigationsRequestStatus,
}) => {
    const { t } = useTranslation();

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: Investigation
    ): ReactElement => {
        if (data.anomaly) {
            const searchParams = new URLSearchParams(
                data.uiMetadata[SavedStateKeys.QUERY_SEARCH_STRING]
            );
            searchParams.set(INVESTIGATION_ID_QUERY_PARAM, data.id.toString());
            const url = `${getRootCauseAnalysisForAnomalyInvestigateV2Path(
                data.anomaly.id as number
            )}?${searchParams.toString()}`;

            return (
                <Link href={url} target="_blank">
                    <Grid container alignItems="center">
                        <Grid item>{cellValue}</Grid>
                        <Grid item>
                            <OpenInNewIcon fontSize="small" />
                        </Grid>
                    </Grid>
                </Link>
            );
        }

        /**
         * In the rare event that anomaly data is missing in the Investigation
         * object, just render the name
         */
        return <span>{cellValue}</span>;
    };

    const investigationsColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 0,
            flex: 1.5,
            sortable: true,
            customCellRenderer: renderLink,
            // eslint-disable-next-line react/display-name
            customCellTooltipRenderer: () => (
                <span>{t("label.view-investigation")}</span>
            ),
        },
        {
            key: "createdBy.principal",
            dataKey: "createdBy.principal",
            header: t("label.created-by"),
            cellTooltip: false,
            minWidth: 0,
            flex: 1,
            sortable: true,
        },
        {
            key: "created",
            dataKey: "created",
            header: t("label.created"),
            cellTooltip: false,
            minWidth: 0,
            flex: 1,
            sortable: true,
            customCellRenderer: (value: Record<string, unknown>) =>
                formatDateAndTimeV1(Number(value)),
        },
        {
            key: "updated",
            dataKey: "updated",
            header: t("label.last-updated"),
            cellTooltip: false,
            minWidth: 0,
            flex: 1,
            sortable: true,
            customCellRenderer: (value: Record<string, unknown>) =>
                formatDateAndTimeV1(Number(value)),
        },
    ];

    return (
        <LoadingErrorStateSwitch
            wrapInCard
            isError={getInvestigationsRequestStatus === ActionStatus.Error}
            isLoading={
                getInvestigationsRequestStatus === ActionStatus.Working ||
                getInvestigationsRequestStatus === ActionStatus.Initial
            }
        >
            <EmptyStateSwitch
                emptyState={<></>}
                isEmpty={!!investigations && investigations.length === 0}
            >
                <PageContentsCardV1 disablePadding fullHeight>
                    <DataGridV1<Investigation>
                        disableSearch
                        disableSelection
                        hideBorder
                        hideToolbar
                        columns={investigationsColumns}
                        data={investigations as Investigation[]}
                        rowKey="id"
                        scroll={DataGridScrollV1.Body}
                    />
                </PageContentsCardV1>
            </EmptyStateSwitch>
        </LoadingErrorStateSwitch>
    );
};
