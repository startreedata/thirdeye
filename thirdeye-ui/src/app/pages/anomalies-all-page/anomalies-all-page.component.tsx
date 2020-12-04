import { Grid } from "@material-ui/core";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { AnomalyCardData } from "../../components/anomaly-card/anomaly-card.interfaces";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { Search } from "../../components/search/search.component";
import { getAllAnomalies } from "../../rest/anomaly-rest/anomaly-rest";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs-store/application-breadcrumbs-store";
import {
    filterAnomalies,
    getAnomalyCardDatas,
} from "../../utils/anomaly-util/anomaly-util";
import { getAnomaliesAllPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [anomalies, setAnomalies] = useState<AnomalyCardData[]>([]);
    const [filteredAnomalies, setfilteredAnomalies] = useState<
        AnomalyCardData[]
    >([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                path: getAnomaliesAllPath(),
            },
        ]);
    }, []);

    useEffect(() => {
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        let anomalies: AnomalyCardData[] = [];
        try {
            anomalies = getAnomalyCardDatas(await getAllAnomalies());
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } finally {
            setAnomalies(anomalies);
            setfilteredAnomalies(filterAnomalies(anomalies, searchWords));
        }
    };

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
                            syncSearchWithURL
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
