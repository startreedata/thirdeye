import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { useNotificationProviderV1 } from "../../platform/components";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { handleCreateAlertClickGenerator } from "../../utils/anomalies/anomalies.util";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import { AlertsEditBasePage } from "../alerts-update-page/alerts-edit-base-page.component";
import { AlertsCreatePageProps } from "./alerts-create-page.interfaces";

export const AlertsCreateBasePage: FunctionComponent<AlertsCreatePageProps> = ({
    startingAlertConfiguration,
}) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);

    const handleCreateAlertClick = useMemo(() => {
        return handleCreateAlertClickGenerator(notify, t, (savedAlert) =>
            navigate(getAlertsAlertPath(savedAlert.id))
        );
    }, [navigate, notify, t]);

    return (
        <AlertsEditBasePage
            pageTitle={t("label.create-entity", {
                entity: t("label.alert"),
            })}
            selectedSubscriptionGroups={subscriptionGroups}
            startingAlertConfiguration={startingAlertConfiguration}
            submitButtonLabel={t("label.create-entity", {
                entity: t("label.alert"),
            })}
            onSubmit={(alert) =>
                handleCreateAlertClick(alert, subscriptionGroups)
            }
            onSubscriptionGroupChange={setSubscriptionGroups}
        />
    );
};
