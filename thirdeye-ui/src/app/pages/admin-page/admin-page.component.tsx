import { Grid } from "@material-ui/core";
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { RecentFailures } from "../../components/admin-page/recent-failures/recent-failures.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageContentsGridV1, PageV1 } from "../../platform/components";

export const AdminPage: FunctionComponent = () => {
    const { t } = useTranslation();

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                title={t("label.admin-dashboard")}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <RecentFailures />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
