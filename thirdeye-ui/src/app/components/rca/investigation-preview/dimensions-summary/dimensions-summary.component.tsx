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
import {
    Box,
    Card,
    CardHeader,
    Checkbox,
    Chip,
    Grid,
    IconButton,
    List,
    ListItem,
    ListItemIcon,
    ListItemSecondaryAction,
} from "@material-ui/core";
import DeleteOutlineIcon from "@material-ui/icons/DeleteOutline";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { serializeKeyValuePair } from "../../../../utils/params/params.util";
import { EmptyStateSwitch } from "../../../page-states/empty-state-switch/empty-state-switch.component";
import { getColorForDimensionCombo } from "../investigation-preview.utils";
import { DimensionsSummaryProps } from "./dimensions-summary.interfaces";

export const DimensionsSummary: FunctionComponent<DimensionsSummaryProps> = ({
    availableDimensionCombinations,
    selectedDimensionCombinations,
    onCheckBoxClick,
    onDeleteClick,
}) => {
    const { t } = useTranslation();

    return (
        <Box position="sticky" top="10px">
            <Card>
                <CardHeader title={t("label.dimensions-summary")} />
                <List disablePadding>
                    <EmptyStateSwitch
                        emptyState={
                            <ListItem divider>
                                <Box p={3} textAlign="center" width="100%">
                                    {t("message.no-dimensions-selected-yet")}
                                </Box>
                            </ListItem>
                        }
                        isEmpty={isEmpty(availableDimensionCombinations)}
                    >
                        {availableDimensionCombinations.map(
                            (dimensionCombination) => {
                                const serializedStr =
                                    serializeKeyValuePair(dimensionCombination);
                                const checked =
                                    selectedDimensionCombinations.has(
                                        serializedStr
                                    );
                                const append = checked ? "-selected" : " ";
                                const color =
                                    getColorForDimensionCombo(
                                        dimensionCombination
                                    );

                                return (
                                    <ListItem
                                        dense
                                        divider
                                        key={serializedStr + append}
                                        onClick={() => {
                                            onCheckBoxClick(
                                                dimensionCombination,
                                                !checked
                                            );
                                        }}
                                    >
                                        <ListItemIcon>
                                            <Checkbox
                                                disableRipple
                                                checked={checked}
                                                edge="start"
                                                tabIndex={-1}
                                            />
                                        </ListItemIcon>
                                        <Box pb={1} pr={1} pt={1}>
                                            <Grid container spacing={1}>
                                                {dimensionCombination.map(
                                                    (kv) => {
                                                        return (
                                                            <Grid
                                                                item
                                                                key={`${kv.key}=${kv.value}`}
                                                            >
                                                                <Chip
                                                                    label={`${kv.key}=${kv.value}`}
                                                                    size="small"
                                                                    style={{
                                                                        borderColor:
                                                                            color,
                                                                        color: color,
                                                                    }}
                                                                />
                                                            </Grid>
                                                        );
                                                    }
                                                )}
                                            </Grid>
                                        </Box>
                                        <ListItemSecondaryAction>
                                            <IconButton
                                                color="secondary"
                                                edge="end"
                                                onClick={() =>
                                                    onDeleteClick(
                                                        dimensionCombination
                                                    )
                                                }
                                            >
                                                <DeleteOutlineIcon />
                                            </IconButton>
                                        </ListItemSecondaryAction>
                                    </ListItem>
                                );
                            }
                        )}
                    </EmptyStateSwitch>
                </List>
            </Card>
        </Box>
    );
};
