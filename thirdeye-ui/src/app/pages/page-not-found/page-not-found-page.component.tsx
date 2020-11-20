import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { PageNotFoundIndicator } from "../../components/page-not-found-indicator/page-not-found-indicator.component";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { getPageNotFoundPath } from "../../utils/route/routes-util";

export const PageNotFoundPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.page-not-found"),
                path: getPageNotFoundPath(),
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
