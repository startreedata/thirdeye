import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { getUniqueAlertLists } from "../../utils/alerts/alerts.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { ChipFilter } from "../quick-filter-chip/quick-filter-chip.component";
import { FilterOption } from "../quick-filter-chip/quick-filter-chip.interfaces";
import { useAnomalyQuickFilterStyles } from "./anomaly-quick-filters.styles";

export const AnomalyQuickFilters: FunctionComponent = () => {
    const classes = useAnomalyQuickFilterStyles();
    const { notify } = useNotificationProviderV1();
    const [alertOptions, setAlertOptions] = useState<Array<FilterOption>>([]);
    const { t } = useTranslation();

    const [searchParams, setSearchParams] = useSearchParams();

    useEffect(() => {
        fetchAlerts();
    }, []);

    const fetchAlerts = async (): Promise<void> => {
        try {
            const alerts = await getAllAlerts();
            setAlertOptions(getUniqueAlertLists(alerts));
        } catch (error) {
            const errorMsgs = getErrorMessages(error as AxiosError);

            !isEmpty(errorMsgs)
                ? errorMsgs.map((msg) => notify(NotificationTypeV1.Error, msg))
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.alerts"),
                      })
                  );
        }
    };

    const handleAlertFilter = (filter?: FilterOption): void => {
        if (!filter) {
            searchParams.delete("alert");
        } else {
            searchParams.set("alert", filter.id + "");
        }

        setSearchParams(searchParams);
    };

    const alertValue = useMemo(
        () => ({
            id: parseInt(searchParams.get("alert") || ""),
            label: "",
        }),
        [searchParams]
    );

    return (
        <div className={classes.root}>
            <ChipFilter
                label="Alert"
                name="alert"
                options={alertOptions}
                value={alertValue}
                onFilter={handleAlertFilter}
            />
        </div>
    );
};
