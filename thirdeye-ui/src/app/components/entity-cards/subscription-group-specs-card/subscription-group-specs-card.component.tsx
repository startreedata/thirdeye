/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
                    <Typography variant="h6">{t("label.groups")}</Typography>
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
