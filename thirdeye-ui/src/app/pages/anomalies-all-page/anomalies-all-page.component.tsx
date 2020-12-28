import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { AnomalyCardData } from "../../components/anomaly-card/anomaly-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import {
    deleteAnomaly,
    getAllAnomalies,
} from "../../rest/anomaly-rest/anomaly-rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    filterAnomalies,
    getAnomalyCardDatas,
} from "../../utils/anomaly-util/anomaly-util";
import { getAnomaliesAllPath } from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";

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
        fetchData();
    }, []);

    useEffect(() => {
        // Fetched data or search changed, reset
        setFilteredAnomalyCardDatas(
            filterAnomalies(anomalyCardDatas, searchWords)
        );
    }, [anomalyCardDatas, searchWords]);

    const fetchData = (): void => {
        let fetchedAnomalyCardDatas: AnomalyCardData[] = [];

        getAllAnomalies()
            .then((anomalies: Anomaly[]): void => {
                fetchedAnomalyCardDatas = getAnomalyCardDatas(anomalies);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setAnomalyCardDatas(fetchedAnomalyCardDatas);

                setLoading(false);
            });
    };

    const onDeleteAnomaly = (anomalyCardData: AnomalyCardData): void => {
        if (!anomalyCardData) {
            return;
        }

        // Delete
        deleteAnomaly(anomalyCardData.id)
            .then((anomaly: Anomaly): void => {
                // Remove deleted anomaly from fetched anomalies
                removeAnomalyCardData(anomaly);

                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.anomaly") }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.anomaly") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const removeAnomalyCardData = (anomaly: Anomaly): void => {
        if (!anomaly) {
            return;
        }

        setAnomalyCardDatas((anomalyCardDatas) =>
            anomalyCardDatas.filter(
                (anomalyCardData: AnomalyCardData): boolean => {
                    return anomalyCardData.id !== anomaly.id;
                }
            )
        );
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
            <PageContents centered title={t("label.anomalies")}>
                <Grid container>
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
                                        onDelete={onDeleteAnomaly}
                                    />
                                </Grid>
                            )
                        )}
                </Grid>

                {/* No data available message */}
                {isEmpty(filteredAnomalyCardDatas) && <NoDataIndicator />}
            </PageContents>
        </PageContainer>
    );
};
