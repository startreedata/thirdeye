import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContents } from "../../components/page-contents/page-contents.component";

export const MetricsDetailsPage: FunctionComponent = () => {
    const { t } = useTranslation();

    return <PageContents title={t("label.metrics")} />;
};
