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
import { Box } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageContentsSpacerV1Props } from "./page-contents-spacer-v1.interfaces";

export const PageContentsSpacerV1: FunctionComponent<PageContentsSpacerV1Props> =
    ({ className, ...otherProps }) => {
        return (
            <Box
                {...otherProps}
                className={classNames(className, "page-contents-spacer-v1")}
                marginBottom={3}
                width="100%"
            />
        );
    };
