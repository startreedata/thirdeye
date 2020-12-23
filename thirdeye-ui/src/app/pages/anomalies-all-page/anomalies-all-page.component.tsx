import { Grid } from "@material-ui/core";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { AnomalyCardData } from "../../components/anomaly-card/anomaly-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import { getAllAnomalies } from "../../rest/anomaly-rest/anomaly-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    filterAnomalies,
    getAnomalyCardDatas,
} from "../../utils/anomaly-util/anomaly-util";
import { getAnomaliesAllPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [anomalyCardDatas, setAnomalyCardDatas] = useState<AnomalyCardData[]>(
        []
    );
    const [filteredAnomalyCardDatas, setFilteredAnomalyCardDatas] = useState<
        AnomalyCardData[]
    >([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                pathFn: getAnomaliesAllPath,
            },
        ]);
    }, []);

    useEffect(() => {
        // Fetch data
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    useEffect(() => {
        // Fetched data, or search changed, reset
        setFilteredAnomalyCardDatas(
            filterAnomalies(anomalyCardDatas, searchWords)
        );
    }, [anomalyCardDatas, searchWords]);

    const fetchData = async (): Promise<void> => {
        let fetchedAnomalyCardDatas: AnomalyCardData[] = [];
        try {
            fetchedAnomalyCardDatas = getAnomalyCardDatas(
                await getAllAnomalies()
            );
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } finally {
            setAnomalyCardDatas(fetchedAnomalyCardDatas);
        }
    };

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents contentsCenterAlign title={t("label.anomalies")}>
                <Grid container md={12}>
                    {/* Search */}
                    <Grid item md={12}>
                        <SearchBar
                            autoFocus
                            setSearchQueryString
                            label={t("label.search-anomalies")}
                            searchStatusLabel={t("label.search-count", {
                                count: filteredAnomalyCardDatas
                                    ? filteredAnomalyCardDatas.length
                                    : 0,
                                total: anomalyCardDatas
                                    ? anomalyCardDatas.length
                                    : 0,
                            })}
                            onChange={setSearchWords}
                        />
                    </Grid>

                    {/* Anomalies */}
                    {filteredAnomalyCardDatas &&
                        filteredAnomalyCardDatas.map(
                            (filteredAnomalyCardData, index) => (
                                <Grid item key={index} md={12}>
                                    <AnomalyCard
                                        anomaly={filteredAnomalyCardData}
                                        searchWords={searchWords}
                                    />
                                </Grid>
                            )
                        )}
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
