import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteAnomaly,
    getAnomaliesByTime,
} from "../../rest/anomalies/anomalies.rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    filterAnomalies,
    getUiAnomalies,
} from "../../utils/anomalies/anomalies.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [uiAnomalys, setUiAnomalys] = useState<UiAnomaly[]>([]);
    const [filteredUiAnomalys, setFilteredUiAnomalys] = useState<UiAnomaly[]>(
        []
    );
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range changed, fetch anomalies
        fetchAnomaliesByTime();
    }, [timeRangeDuration]);

    useEffect(() => {
        // Fetched anomalies or search changed, reset
        setFilteredUiAnomalys(filterAnomalies(uiAnomalys, searchWords));
    }, [uiAnomalys, searchWords]);

    const onDeleteAnomaly = (uiAnomaly: UiAnomaly): void => {
        if (!uiAnomaly) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiAnomaly.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteAnomalyConfirmation(uiAnomaly);
            },
        });
    };

    const onDeleteAnomalyConfirmation = (uiAnomaly: UiAnomaly): void => {
        if (!uiAnomaly) {
            return;
        }

        deleteAnomaly(uiAnomaly.id)
            .then((anomaly: Anomaly): void => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.anomaly") }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted anomaly from fetched anomalies
                removeUiAnomaly(anomaly);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.anomaly") }),
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
                setUiAnomalys(getUiAnomalies(anomalies));
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

    const removeUiAnomaly = (anomaly: Anomaly): void => {
        if (!anomaly) {
            return;
        }

        setUiAnomalys((uiAnomalys) =>
            uiAnomalys.filter((uiAnomaly: UiAnomaly): boolean => {
                return uiAnomaly.id !== anomaly.id;
            })
        );
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents centered hideAppBreadcrumbs title={t("label.anomalies")}>
            <Grid container>
                {/* Search */}
                <Grid item sm={12}>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-anomalies")}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiAnomalys ? filteredUiAnomalys.length : 0,
                            uiAnomalys ? uiAnomalys.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>

                {/* Anomalies */}
                {filteredUiAnomalys &&
                    filteredUiAnomalys.map((filteredUiAnomaly, index) => (
                        <Grid item key={index} sm={12}>
                            <AnomalyCard
                                showViewDetails
                                searchWords={searchWords}
                                uiAnomaly={filteredUiAnomaly}
                                onDelete={onDeleteAnomaly}
                            />
                        </Grid>
                    ))}
            </Grid>

            {/* No data available message */}
            {isEmpty(filteredUiAnomalys) && isEmpty(searchWords) && (
                <NoDataIndicator />
            )}

            {/* No search results available message */}
            {isEmpty(filteredUiAnomalys) && !isEmpty(searchWords) && (
                <NoDataIndicator text={t("message.no-search-results")} />
            )}
        </PageContents>
    );
};
