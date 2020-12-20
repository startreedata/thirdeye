import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationToolbar } from "../../components/configuration-toolbar/configuration-toolbar.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { getConfigurationSubscriptionGroupsAllPath } from "../../utils/routes-util/routes-util";

export const ConfigurationSubscriptionGroupsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                path: getConfigurationSubscriptionGroupsAllPath(),
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
            <PageContainer toolbar={<ConfigurationToolbar />}>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer toolbar={<ConfigurationToolbar />}>
            <PageContents
                contentsCenterAlign
                title={t("label.subscription-groups")}
            />
        </PageContainer>
    );
};
