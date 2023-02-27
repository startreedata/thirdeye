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

import { Icon } from "@iconify/react";
import {
    Box,
    Card,
    CardContent,
    CardHeader,
    Drawer,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { HelpDrawerV1Props } from "./help-drawer-v1.interfaces";
import { useHelperDrawerV1Styles } from "./help-drawer-v1.styles";

export const HelpDrawerV1: FunctionComponent<HelpDrawerV1Props> = ({
    isOpen,
    handleClose,
    title,
    cards,
    children,
}) => {
    const classes = useHelperDrawerV1Styles();

    return (
        <Drawer
            PaperProps={{
                className: classes.drawerPaper,
            }}
            anchor="right"
            open={isOpen}
            onClose={handleClose}
        >
            <Box pb={2}>
                <Card>
                    <CardContent>
                        <Box
                            alignItems="center"
                            display="flex"
                            justifyContent="space-between"
                        >
                            <Typography variant="h5">{title}</Typography>
                            <Icon
                                cursor="pointer"
                                fontSize={24}
                                icon="ic:round-close"
                                onClick={handleClose}
                            />
                        </Box>
                    </CardContent>
                </Card>
            </Box>
            {!!cards &&
                cards.map((card, idx) => (
                    <Box key={idx} px={2} py={1}>
                        <Card variant="outlined">
                            <CardHeader title={card.title} />
                            <CardContent>{card.body}</CardContent>
                        </Card>
                    </Box>
                ))}
            {children}
        </Drawer>
    );
};
