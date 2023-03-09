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
import React, { FunctionComponent } from "react";
import { PageContentsCardV1 } from "../../../platform/components";
import { SummaryContainerCardProps } from "./summary-container-card.interfaces";

export const SummaryContainerCard: FunctionComponent<SummaryContainerCardProps> =
    ({ items, gridContainerProps, rootCardProps }) => {
        return (
            <PageContentsCardV1 {...rootCardProps}>
                <Grid container spacing={8} {...gridContainerProps}>
                    {items.map(({ key, content, ...gridItemProps }, idx) => (
                        <Grid item key={key || idx} {...gridItemProps}>
                            {content}
                        </Grid>
                    ))}
                </Grid>
            </PageContentsCardV1>
        );
    };
