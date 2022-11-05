import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { NotificationSpec } from "../../../rest/dto/subscription-group.interfaces";
import { specTypeToUIConfig } from "../../subscription-group-wizard/groups-editor/groups-editor.utils";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { SubscriptionGroupSpecsCardProps } from "./subscription-group-specs-card.interfaces";

export const SubscriptionGroupSpecsCard: FunctionComponent<
    SubscriptionGroupSpecsCardProps
> = ({ specs }) => {
    const { t } = useTranslation();

    return (
        <PageContentsCardV1>
            <Grid container>
                <Grid item xs={12}>
                    <Typography variant="h6">{t("label.channels")}</Typography>
                </Grid>
                {specs.length > 0 &&
                    specs.map((spec, idx) => {
                        const uiConfigForSpecType =
                            specTypeToUIConfig[spec.type];

                        return (
                            <Grid
                                item
                                key={`${spec.type}-${idx}`}
                                sm={6}
                                xs={12}
                            >
                                <NameValueDisplayCard<NotificationSpec>
                                    name={t(
                                        uiConfigForSpecType.internationalizationString
                                    )}
                                    valueRenderer={(value) =>
                                        React.createElement(
                                            uiConfigForSpecType.reviewComponent,
                                            { configuration: value }
                                        )
                                    }
                                    values={[spec]}
                                />
                            </Grid>
                        );
                    })}

                {specs.length === 0 && (
                    <Grid item xs={12}>
                        {t("message.no-notifications-groups")}
                    </Grid>
                )}
            </Grid>
        </PageContentsCardV1>
    );
};
