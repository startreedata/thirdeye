import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { Search } from "../../components/search/search.component";
import { getAllAnomalies } from "../../rest/anomaly/anomaly-rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { filterAnomalies } from "../../utils/anomaly/anomaly-util";
import { getAnomaliesAllPath } from "../../utils/route/routes-util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
    const [filteredAnomalies, setfilteredAnomalies] = useState<Anomaly[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                path: getAnomaliesAllPath(),
            },
        ]);
    }, [setPageBreadcrumbs, t]);

    useEffect(() => {
        const fetchData = async (): Promise<void> => {
            const alerts = await getAllAnomalies();

            setAnomalies(alerts);
            setfilteredAnomalies(filterAnomalies(alerts, searchWords));

            setLoading(false);
        };

        fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // Doesn't need to depend on searchWords

    const onSearch = (searchWords: string[]): void => {
        setSearchWords(searchWords);
        setfilteredAnomalies(filterAnomalies(anomalies, searchWords));
    };

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
                <Grid container>
                    <Grid item md={12}>
                        <Search
                            autoFocus
                            searchStatusText={t("label.search-count", {
                                count: filteredAnomalies.length,
                                total: anomalies.length,
                            })}
                            onChange={onSearch}
                        />
                    </Grid>

                    {filteredAnomalies.map((anomaly) => (
                        <Grid item key={anomaly.id} md={12}>
                            <AnomalyCard
                                anomaly={anomaly}
                                searchWords={searchWords}
                            />
                        </Grid>
                    ))}
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
