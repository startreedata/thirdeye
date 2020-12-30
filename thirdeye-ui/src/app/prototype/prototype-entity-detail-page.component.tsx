import React, { FunctionComponent, useEffect } from "react";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageContents } from "../components/page-contents/page-contents.component";
import { useAppBreadcrumbsStore } from "../store/app-breadcrumbs-store/app-breadcrumbs-store";

export const PrototypeEntityDetailPage: FunctionComponent = () => {
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: "ENTITY DETAIL",
            },
        ]);
    }, []);

    return (
        <PageContainer>
            <PageContents centered hideTimeRange title={"ENTITY DETAIL"} />
        </PageContainer>
    );
};
