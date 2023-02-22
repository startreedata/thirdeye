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

import {
    Card,
    CardContent,
    CardHeader,
    InputLabel,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { SubscriptionGroupViewCardProps } from "./subscription-group-view-card.interface";

export const SubscriptionGroupViewCard: FunctionComponent<SubscriptionGroupViewCardProps> =
    ({ header, rows }) => {
        return (
            <Card>
                <CardHeader title={header} />
                <CardContent>
                    {rows.map((row) => (
                        <InputSection
                            gridContainerProps={{ spacing: 8 }}
                            inputComponent={
                                <Typography variant="body2">
                                    {row.value}
                                </Typography>
                            }
                            key={JSON.stringify(row)}
                            labelComponent={
                                <InputLabel>{row.label}</InputLabel>
                            }
                        />
                    ))}
                </CardContent>
            </Card>
        );
    };
