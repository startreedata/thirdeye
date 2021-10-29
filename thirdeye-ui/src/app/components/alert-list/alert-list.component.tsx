import { Grid } from "@material-ui/core";
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { filterAlerts } from "../../utils/alerts/alerts.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import { AlertCard } from "../entity-cards/alert-card/alert-card.component";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { AlertListProps } from "./alert-list.interfaces";

export const AlertList: FunctionComponent<AlertListProps> = (
    props: AlertListProps
) => {
    const [filteredUiAlerts, setFilteredUiAlerts] = useState<UiAlert[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();

    useEffect(() => {
        // Input alerts or search changed, reset
        setFilteredUiAlerts(filterAlerts(props.alerts || [], searchWords));
    }, [props.alerts, searchWords]);

    return (
        <>
            <Grid container>
                {/* Search */}
                {!props.hideSearchBar && (
                    <Grid item xs={12}>
                        <SearchBar
                            autoFocus
                            setSearchQueryString
                            searchLabel={t("label.search-entity", {
                                entity: t("label.alerts"),
                            })}
                            searchStatusLabel={getSearchStatusLabel(
                                filteredUiAlerts ? filteredUiAlerts.length : 0,
                                props.alerts ? props.alerts.length : 0
                            )}
                            onChange={setSearchWords}
                        />
                    </Grid>
                )}

                {/* Alerts */}
                {props.alerts &&
                    filteredUiAlerts &&
                    filteredUiAlerts.map((filteredUiAlert, index) => (
                        <Grid item key={index} sm={12}>
                            <AlertCard
                                showViewDetails
                                searchWords={searchWords}
                                uiAlert={filteredUiAlert}
                                onChange={props.onChange}
                                onDelete={props.onDelete}
                            />
                        </Grid>
                    ))}
            </Grid>

            {/* Loading indicator */}
            {!props.alerts && <AppLoadingIndicatorV1 />}

            {/* No data available message */}
            {props.alerts &&
                isEmpty(filteredUiAlerts) &&
                isEmpty(searchWords) && <NoDataIndicator />}

            {/* No search results available message */}
            {props.alerts &&
                isEmpty(filteredUiAlerts) &&
                !isEmpty(searchWords) && (
                    <NoDataIndicator text={t("message.no-search-results")} />
                )}
        </>
    );
};
