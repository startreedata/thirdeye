import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import AnomaliesCard from "../../components/anomalies/anomalies-card.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { getAllAnomalies } from "../../rest/anomaly/anomaly-rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { getAnomaliesAllPath } from "../../utils/route/routes-util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [anomalies, setAnomalies] = useState<Anomaly[] | undefined>();
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async (): Promise<void> => {
        const anomalies = await getAllAnomalies();
        setAnomalies(anomalies);
        setLoading(false);
    };

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                path: getAnomaliesAllPath(),
            },
        ]);

        // setLoading(false);
    }, [setPageBreadcrumbs, t]);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents centerAlign title={t("label.anomalies")}>
                {anomalies?.map((anomalie) => (
                    <AnomaliesCard
                        data={anomalie}
                        key={anomalie.id}
                        mode="list"
                    />
                ))}
            </PageContents>
        </PageContainer>
    );
};
