import { Link, Typography } from "@material-ui/core";
import React, { FunctionComponent, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridScrollV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { formatDateAndTimeV1 } from "../../platform/utils";
import { Investigation } from "../../rest/dto/rca.interfaces";
import { getRootCauseAnalysisSavedInvestigationPath } from "../../utils/routes/routes.util";
import { InvestigationsListProps } from "./investigations-list.interfaces";

export const InvestigationsList: FunctionComponent<InvestigationsListProps> = ({
    investigations,
}) => {
    const { t } = useTranslation();

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: Investigation
    ): ReactElement => {
        return (
            <Link href={getRootCauseAnalysisSavedInvestigationPath(data.id)}>
                {cellValue}
            </Link>
        );
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
            customCellTooltipRenderer: (
                _: Record<string, unknown>,
                data: Investigation
            ) => <span>{data.text}</span>,
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
