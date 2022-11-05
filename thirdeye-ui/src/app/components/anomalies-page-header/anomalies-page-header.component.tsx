import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import {
    AppRouteRelative,
    getAnomaliesAllPath,
} from "../../utils/routes/routes.util";
import { Crumb } from "../breadcrumbs/breadcrumbs.interfaces";
import { PageHeader } from "../page-header/page-header.component";

export const AnomaliesPageHeader: FunctionComponent = () => {
    const { search } = useLocation();
    const { pathname } = useLocation();
    const { t } = useTranslation();
    const crumbs: Crumb[] = [
        {
            label: t("label.anomalies"),
            link: getAnomaliesAllPath(),
        },
    ];

    if (pathname.indexOf(AppRouteRelative.ANOMALIES_METRICS_REPORT) > -1) {
        crumbs.push({
            label: t("label.metrics-report"),
        });
    } else if (pathname.indexOf(AppRouteRelative.ANOMALIES_LIST) > -1) {
        crumbs.push({
            label: t("label.anomalies-list"),
        });
    }

    return (
        <PageHeader
            showCreateButton
            breadcrumbs={crumbs}
            subNavigation={[
                {
                    link: `${AppRouteRelative.ANOMALIES_LIST}${search}`,
                    label: t("label.anomalies-list"),
                },
                {
                    link: `${AppRouteRelative.ANOMALIES_METRICS_REPORT}${search}`,
                    label: t("label.metrics-report"),
                },
            ]}
            subNavigationSelected={
                pathname.indexOf(AppRouteRelative.ANOMALIES_LIST) > -1
                    ? 0
                    : pathname.indexOf(AppRouteRelative.METRICS_REPORT) > -1
                    ? 1
                    : undefined
            }
            title={t("label.anomalies")}
        />
    );
};
