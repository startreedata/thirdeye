import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { filterAnomalies } from "../../utils/anomalies/anomalies.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import { AnomalyCard } from "../entity-cards/anomaly-card/anomaly-card.component";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { AnomalyListProps } from "./anomaly-list.interfaces";

export const AnomalyList: FunctionComponent<AnomalyListProps> = (
    props: AnomalyListProps
) => {
    const [filteredUiAnomalies, setFilteredUiAnomalies] = useState<UiAnomaly[]>(
        []
    );
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();

    useEffect(() => {
        // Input anomalies or search changed, reset
        setFilteredUiAnomalies(
            filterAnomalies(props.uiAnomalies as UiAnomaly[], searchWords)
        );
    }, [props.uiAnomalies, searchWords]);

    const handleAnomalyDelete = (uiAnomaly: UiAnomaly): void => {
        if (!uiAnomaly) {
            return;
        }

        props.onDelete && props.onDelete(uiAnomaly);
    };

    return (
        <Grid container>
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item xs={12}>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.anomalies"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiAnomalies
                                ? filteredUiAnomalies.length
                                : 0,
                            props.uiAnomalies ? props.uiAnomalies.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Anomalies */}
            {filteredUiAnomalies &&
                filteredUiAnomalies.map((filteredUiAnomaly, index) => (
                    <Grid item key={index} sm={12}>
                        <AnomalyCard
                            showViewDetails
                            searchWords={searchWords}
                            uiAnomaly={filteredUiAnomaly}
                            onDelete={handleAnomalyDelete}
                        />
                    </Grid>
                ))}

            {/* No data available message */}
            {isEmpty(filteredUiAnomalies) && isEmpty(searchWords) && (
                <NoDataIndicator />
            )}

            {/* No search results available message */}
            {isEmpty(filteredUiAnomalies) && !isEmpty(searchWords) && (
                <NoDataIndicator text={t("message.no-search-results")} />
            )}
        </Grid>
    );
};
