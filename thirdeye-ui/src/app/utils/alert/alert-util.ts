import i18n from "i18next";
import { Alert } from "../../rest/dto/alert.interfaces";

export const filterAlerts = (
    alerts: Alert[],
    searchWords: string[]
): Alert[] => {
    const filteredAlerts = new Set<Alert>();

    if (!alerts || alerts.length === 0) {
        // No alerts available, return empty result
        return Array.from(filteredAlerts);
    }

    if (!searchWords || searchWords.length === 0) {
        // No search words available, return original alerts
        return alerts;
    }

    for (let alertIndex = 0; alertIndex < alerts.length; alertIndex++) {
        for (
            let searchWordIndex = 0;
            searchWordIndex < searchWords.length;
            searchWordIndex++
        ) {
            // Try and match relevant alert property values to search words
            if (
                // Active, inactive keywords
                i18n
                    .t(
                        alerts[alertIndex].active
                            ? "label.active"
                            : "label.inactive"
                    )
                    .toLowerCase() ===
                    searchWords[searchWordIndex].toLowerCase() ||
                // Alert name
                alerts[alertIndex].name
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Alert description
                alerts[alertIndex].description
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Alert owner
                alerts[alertIndex].owner.principal
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1
            ) {
                filteredAlerts.add(alerts[alertIndex]);
            }
        }
    }

    return Array.from(filteredAlerts);
};
