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
import { Link, Typography } from "@material-ui/core";
import React from "react";
import { Link as RouterLink } from "react-router-dom";
import { IconLinkProps } from "./icon-link.interfaces";
import { useIconLinkStyles } from "./icon-link.styles";

const IconLink: React.FC<IconLinkProps> = ({ label, icon, route }) => {
    const styles = useIconLinkStyles();

    return (
        <Link
            classes={{ root: styles.iconLink }}
            component={RouterLink}
            to={route}
        >
            {icon}
            <Typography classes={{ root: styles.iconLinkText }}>
                {label}
            </Typography>
        </Link>
    );
};

export default IconLink;
