import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";

export const AlertsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: "ALERT_NAME",
                path: "",
            },
            {
                text: t("label.update"),
                path: "",
            },
        ]);

        setLoading(false);
    }, [setPageBreadcrumbs, t]);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageLoadingIndicator />
        </PageContainer>
    );
};
