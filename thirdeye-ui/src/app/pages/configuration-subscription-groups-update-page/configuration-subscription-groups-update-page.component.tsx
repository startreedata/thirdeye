import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AppToolbarConfiguration } from "../../components/app-toolbar-configuration/app-toolbar-configuration.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";

export const ConfigurationSubscriptionGroupsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: "SUBSCRIPTION GROUP",
            },
            {
                text: t("label.update"),
            },
        ]);
    }, []);

    useEffect(() => {
        const init = async (): Promise<void> => {
            setLoading(false);
        };

        init();
    }, []);

    if (loading) {
        return (
            <PageContainer appToolbar={<AppToolbarConfiguration />}>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer appToolbar={<AppToolbarConfiguration />}>
            <PageContents contentsCenterAlign title={t("label.update")} />
        </PageContainer>
    );
};
