import { useMediaQuery, useTheme } from "@material-ui/core";
import {
    PageHeaderActionsV1,
    PageHeaderTabsV1,
    PageHeaderTabV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AppRoute } from "../../utils/routes/routes.util";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector/time-range-selector.component";
import { ConfigurationPageHeaderProps } from "./configuration-page-header.interfaces";

export const ConfigurationPageHeader: FunctionComponent<ConfigurationPageHeaderProps> = (
    props: ConfigurationPageHeaderProps
) => {
    const { t } = useTranslation();
    const theme = useTheme();
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
        refreshTimeRange,
    } = useTimeRange();

    const screenWidthSmUp = useMediaQuery(theme.breakpoints.up("sm"));

    return (
        <PageHeaderV1>
            <PageHeaderTextV1>{t("label.configuration")}</PageHeaderTextV1>
            <PageHeaderActionsV1>
                {/* Time range selector */}
                <TimeRangeSelector
                    hideTimeRange={!screenWidthSmUp}
                    recentCustomTimeRangeDurations={
                        recentCustomTimeRangeDurations
                    }
                    timeRangeDuration={timeRangeDuration}
                    onChange={setTimeRangeDuration}
                    onRefresh={refreshTimeRange}
                />

                {/* Create options button */}
                <CreateMenuButton />
            </PageHeaderActionsV1>

            <PageHeaderTabsV1 selectedIndex={props.selectedIndex}>
                <PageHeaderTabV1 href={AppRoute.SUBSCRIPTION_GROUPS}>
                    {t("label.subscription-groups")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={AppRoute.DATASETS}>
                    {t("label.datasets")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={AppRoute.DATASOURCES}>
                    {t("label.datasources")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={AppRoute.METRICS}>
                    {t("label.metrics")}
                </PageHeaderTabV1>
            </PageHeaderTabsV1>
        </PageHeaderV1>
    );
};
