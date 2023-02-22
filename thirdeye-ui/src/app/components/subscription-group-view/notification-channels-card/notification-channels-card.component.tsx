/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { SubscriptionGroupViewCard } from "../subscription-group-view-card/subscription-group-view-card.component";
import { NotificationChannelsCardProps } from "./notification-channels-card.interface";
import { getCardProps } from "./notification-channels-card.utils";

export const NotificationChannelsCard: FunctionComponent<NotificationChannelsCardProps> =
    ({ activeChannels }) => {
        const { t } = useTranslation();

        const renderCards = useMemo(
            () => activeChannels.map((channel) => getCardProps(channel, t)),
            [activeChannels]
        );

        return (
            <>
                {renderCards.map((cardProps, index) => (
                    <Grid item key={index} xs={12}>
                        <SubscriptionGroupViewCard {...cardProps} />
                    </Grid>
                ))}
            </>
        );
    };
