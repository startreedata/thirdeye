import { Button, Grid, Link } from "@material-ui/core";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import {
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { SubscriptionGroupListV1Props } from "./subscription-group-list-v1.interfaces";

export const SubscriptionGroupListV1: FunctionComponent<SubscriptionGroupListV1Props> = (
    props: SubscriptionGroupListV1Props
) => {
    const { t } = useTranslation();
    const [selectedSubscriptionGroup, setSelectedSubscriptionGroup] = useState<
        DataGridSelectionModelV1<UiSubscriptionGroup>
    >();
    const history = useHistory();

    const handleSubscriptionGroupDelete = (): void => {
        if (!selectedSubscriptionGroup) {
            return;
        }

        const selectedSubScriptionGroupId = selectedSubscriptionGroup
            .rowKeyValues[0] as number;
        const uiSubscriptionGroup = getUiSubscriptionGroup(
            selectedSubScriptionGroupId
        );
        if (!uiSubscriptionGroup) {
            return;
        }

        props.onDelete && props.onDelete(uiSubscriptionGroup);
    };

    const getUiSubscriptionGroup = (id: number): UiSubscriptionGroup | null => {
        if (!props.subscriptionGroups) {
            return null;
        }

        return (
            props.subscriptionGroups.find(
                (subscriptionGroup) => subscriptionGroup.id === id
            ) || null
        );
    };

    const handleSubscriptionGroupEdit = (): void => {
        if (!selectedSubscriptionGroup) {
            return;
        }
        const selectedSubScriptionGroupId = selectedSubscriptionGroup
            .rowKeyValues[0] as number;

        history.push(
            getSubscriptionGroupsUpdatePath(selectedSubScriptionGroupId)
        );
    };

    const isActionButtonDisable = !(
        selectedSubscriptionGroup &&
        selectedSubscriptionGroup.rowKeyValues.length === 1
    );

    const handleSubscriptionGroupViewDetailsById = (id: number): void => {
        history.push(getSubscriptionGroupsViewPath(id));
    };

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: UiSubscriptionGroup
    ): ReactElement => {
        return (
            <Link
                onClick={() => handleSubscriptionGroupViewDetailsById(data.id)}
            >
                {cellValue}
            </Link>
        );
    };

    const subscriptionGroupColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 0,
            flex: 1.5,
            sortable: true,
            customCellRenderer: renderLink,
        },
        {
            key: "cron",
            dataKey: "cron",
            header: t("label.cron"),
            minWidth: 0,
            flex: 1,
        },
        {
            key: "alertCount",
            dataKey: "alertCount",
            header: t("label.subscribed-alerts"),
            minWidth: 0,
            flex: 1,
            sortable: true,
        },
        {
            key: "emailCount",
            dataKey: "emailCount",
            header: t("label.subscribed-emails"),
            minWidth: 0,
            flex: 1,
            sortable: true,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<UiSubscriptionGroup>
                    hideBorder
                    columns={subscriptionGroupColumns}
                    data={props.subscriptionGroups as UiSubscriptionGroup[]}
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.subscription-groups"),
                    })}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleSubscriptionGroupEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            <Grid>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleSubscriptionGroupDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedSubscriptionGroup}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
