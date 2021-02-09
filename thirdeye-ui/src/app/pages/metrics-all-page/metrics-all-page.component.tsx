import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { MetricsList } from "../../components/metrics-list/metrics-list.component";
import { MetricsListData } from "../../components/metrics-list/metrics-list.interfaces";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { getAllMetrics } from "../../rest/metrics-rest/metrics-rest";
import { getMetricTableDatas } from "../../utils/metrics-util/metrics-util";
import { getMetricsAllPath } from "../../utils/routes/routes.util";

export const MetricsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [metricsTableDatas, setMetricsTableDatas] = useState<
        Array<MetricsListData>
    >([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                onClick: (): void => {
                    history.push(getMetricsAllPath());
                },
            },
        ]);

        fetchData();
    }, []);

    const fetchData = async (): Promise<void> => {
        try {
            const data = await getAllMetrics();
            setMetricsTableDatas(getMetricTableDatas(data));
        } catch (error) {
            console.log(error);
        }
        setLoading(false);
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents centered title={t("label.metrics")}>
            <MetricsList metrics={metricsTableDatas} />
        </PageContents>
    );
};
