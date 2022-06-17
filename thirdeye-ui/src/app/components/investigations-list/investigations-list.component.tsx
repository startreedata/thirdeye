import { Box, Grid, Link, Typography } from "@material-ui/core";
import OpenInNewIcon from "@material-ui/icons/OpenInNew";
import React, { FunctionComponent, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridScrollV1,
    DataGridV1,
    PageContentsCardV1,
    SkeletonV1,
} from "../../platform/components";
import { formatDateAndTimeV1 } from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import { INVESTIGATION_ID_QUERY_PARAM } from "../../utils/investigation/investigation.util";
import { getRootCauseAnalysisForAnomalyInvestigatePath } from "../../utils/routes/routes.util";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { useInvestigationListStyles } from "./investigation-list.styles";
import { InvestigationsListProps } from "./investigations-list.interfaces";

export const InvestigationsList: FunctionComponent<InvestigationsListProps> = ({
    investigations,
    getInvestigationsRequestStatus,
}) => {
    const classes = useInvestigationListStyles();
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
            const url = `${getRootCauseAnalysisForAnomalyInvestigatePath(
                data.anomaly.id as number
            )}?${searchParams.toString()}`;

            return (
                <Link href={url} target="_blank">
                    <Grid container alignItems="center">
                        <Grid item>{cellValue}</Grid>
                        <Grid item>
                            <OpenInNewIcon
                                className={classes.iconAlignVertical}
                                fontSize="small"
                            />
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

    if (getInvestigationsRequestStatus === ActionStatus.Working) {
        return (
            <PageContentsCardV1>
                <SkeletonV1 animation="pulse" />
                <SkeletonV1 animation="pulse" />
                <SkeletonV1 animation="pulse" />
            </PageContentsCardV1>
        );
    }

    if (getInvestigationsRequestStatus === ActionStatus.Error) {
        return (
            <PageContentsCardV1>
                <Box pb={20} pt={20}>
                    <NoDataIndicator />
                </Box>
            </PageContentsCardV1>
        );
    }

    if (
        getInvestigationsRequestStatus === ActionStatus.Done &&
        investigations &&
        investigations.length === 0
    ) {
        return (
            <PageContentsCardV1>
                <Box pb={3} pt={3} textAlign="center">
                    <Typography variant="h6">
                        {t("message.no-saved-investigations")}
                    </Typography>
                </Box>
            </PageContentsCardV1>
        );
    }

    return (
        <PageContentsCardV1 disablePadding fullHeight>
            <DataGridV1<Investigation>
                disableSearch
                disableSelection
                hideBorder
                columns={investigationsColumns}
                data={investigations as Investigation[]}
                rowKey="id"
                scroll={DataGridScrollV1.Body}
                toolbarComponent={
                    <Typography variant="h6">
                        {t("label.saved-investigations-anomaly")}
                    </Typography>
                }
            />
        </PageContentsCardV1>
    );
};
