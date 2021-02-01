import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { AnomalyCardData } from "../../components/entity-cards/anomaly-card/anomaly-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteAnomaly,
    getAnomaliesByTime,
} from "../../rest/anomalies-rest/anomalies-rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    filterAnomalies,
    getAnomalyCardDatas,
} from "../../utils/anomalies-util/anomalies-util";
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
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                onClick: (): void => {
                    history.push(getAnomaliesAllPath());
                },
            },
        ]);
    }, []);

    useEffect(() => {
        // Time range changed, fetch anomalies
        fetchAnomaliesByTime();
    }, [timeRangeDuration]);

    useEffect(() => {
        // Fetched anomalies or search changed, reset
        setFilteredAnomalyCardDatas(
            filterAnomalies(anomalyCardDatas, searchWords)
        );
    }, [anomalyCardDatas, searchWords]);

    const onDeleteAnomaly = (anomalyCardData: AnomalyCardData): void => {
        if (!anomalyCardData) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: anomalyCardData.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteAnomalyConfirmation(anomalyCardData);
            },
        });
    };

    const onDeleteAnomalyConfirmation = (
        anomalyCardData: AnomalyCardData
    ): void => {
        if (!anomalyCardData) {
            return;
        }

        deleteAnomaly(anomalyCardData.id)
            .then((anomaly: Anomaly): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.anomaly"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted anomaly from fetched anomalies
                removeAnomalyCardData(anomaly);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.anomaly"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAnomaliesByTime = (): void => {
        getAnomaliesByTime(
            timeRangeDuration.startTime,
            timeRangeDuration.endTime
        )
            .then((anomalies: Anomaly[]): void => {
                setAnomalyCardDatas(getAnomalyCardDatas(anomalies));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setLoading(false);
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
                    <Grid item sm={12}>
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
                                <Grid item key={index} sm={12}>
                                    <AnomalyCard
                                        anomalyCardData={
                                            filteredAnomalyCardData
                                        }
                                        searchWords={searchWords}
                                        onDelete={onDeleteAnomaly}
                                    />
                                </Grid>
                            )
                        )}
                </Grid>

                {/* No data available message */}
                {isEmpty(filteredAnomalyCardDatas) && isEmpty(searchWords) && (
                    <NoDataIndicator />
                )}

                {/* No search results available message */}
                {isEmpty(filteredAnomalyCardDatas) && !isEmpty(searchWords) && (
                    <NoDataIndicator text={t("message.no-search-results")} />
                )}
            </PageContents>
        </PageContainer>
    );
};
