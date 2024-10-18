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
import { Box, makeStyles, Typography } from "@material-ui/core";
import React from "react";

interface InfoBlockProps {
    icon: React.ReactNode;
    title: string;
    value: React.ReactNode;
}

const useStyles = makeStyles((theme) => ({
    root: {
        display: "flex",
    },
    iconContainer: {
        marginBottom: theme.spacing(1),
        color: theme.palette.primary.main,
        marginRight: theme.spacing(1),
    },
    title: {
        color: theme.palette.text.secondary,
        marginBottom: theme.spacing(0.5),
    },
    value: {
        fontWeight: "bold",
    },
}));

export const InfoBlock: React.FC<InfoBlockProps> = ({ icon, title, value }) => {
    const classes = useStyles();

    return (
        <Box className={classes.root}>
            <Box className={classes.iconContainer}>{icon}</Box>
            <Box>
                <Typography className={classes.value} variant="body2">
                    {value}
                </Typography>
                <Typography className={classes.title} variant="caption">
                    {title}
                </Typography>
            </Box>
        </Box>
    );
};
