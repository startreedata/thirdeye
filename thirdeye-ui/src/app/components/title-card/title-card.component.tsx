/*
 * Copyright 2024 StarTree Inc
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
import { Card, CardContent } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { TitleCardProps } from "./title-card.interfaces";
import { titleCardStyles } from "./title-card.styles";

const TitleCard: FunctionComponent<TitleCardProps> = (props) => {
    const classes = titleCardStyles();

    return (
        <Card className={classes.card}>
            <div className={classes.header}>{props.title}</div>
            <CardContent>{props.content}</CardContent>
        </Card>
    );
};

export default TitleCard;
