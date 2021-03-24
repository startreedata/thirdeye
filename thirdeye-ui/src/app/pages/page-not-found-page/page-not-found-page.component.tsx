import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageNotFoundIndicator } from "../../components/page-not-found-indicator/page-not-found-indicator.component";

export const PageNotFoundPage: FunctionComponent = () => {
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    return (
        <PageContents hideHeader title={t("label.page-not-found")}>
            <PageNotFoundIndicator />
        </PageContents>
    );
};
