import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    PageContentsGridV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageNotFoundIndicatorV1,
    PageV1,
} from "../../components/platform-ui/components";

export const PageNotFoundPage: FunctionComponent = () => {
    const { t } = useTranslation();

    return (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>{t("label.page-not-found")}</PageHeaderTextV1>
            </PageHeaderV1>

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    <PageNotFoundIndicatorV1
                        headerText={t("label.404")}
                        messageText={t("message.page-not-found")}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
