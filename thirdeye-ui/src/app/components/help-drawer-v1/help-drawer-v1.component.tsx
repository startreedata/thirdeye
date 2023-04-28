/*
 * Copyright 2023 StarTree Inc
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
import React, { FunctionComponent, useState } from "react";
import { HelpDrawerV1Props } from "./help-drawer-v1.interfaces";
import { useHelperDrawerV1Styles } from "./help-drawer-v1.styles";

/**
 * Convenience wrapper on top of HelpDrawerCoreV1 so consumers do not have
 * to maintain isOpen state
 */
export const HelpDrawerV1: FunctionComponent<HelpDrawerV1Props> = ({
    title,
    cards,
    children,
    trigger,
}) => {
    const classes = useHelperDrawerV1Styles();
    const [isOpen, setIsOpen] = useState<boolean>(false);

    return (
        <>
            {trigger(() => setIsOpen(true))}
            <Drawer
                PaperProps={{
                    className: classes.drawerPaper,
                }}
                anchor="right"
                open={isOpen}
                onClose={() => setIsOpen(false)}
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
                                    onClick={() => setIsOpen(false)}
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
        </>
    );
};
