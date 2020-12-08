import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { PageNotFoundIndicator } from "../../components/page-not-found-indicator/page-not-found-indicator.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";

export const PageNotFoundPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.page-not-found"),
                path: "",
            },
        ]);

        setLoading(false);
    }, []);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents
                centerAlign
                hideTimeRange
                titleCenterAlign
                title={t("label.page-not-found")}
            >
                <PageNotFoundIndicator />
            </PageContents>
        </PageContainer>
    );
};
