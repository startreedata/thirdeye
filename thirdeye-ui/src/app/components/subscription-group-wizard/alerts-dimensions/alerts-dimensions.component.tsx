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
    CardContent,
    Divider,
    FormControlLabel,
    Grid,
    Radio,
    RadioGroup,
    TextField,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertEnumerationItemSelectTable } from "./alert-enumeration-item-select-table/alert-enumeration-item-select-table.component";
import {
    AlertsDimensionsProps,
    FilterChoices,
} from "./alerts-dimensions.interfaces";

export const AlertsDimensions: FunctionComponent<AlertsDimensionsProps> = ({
    alerts,
    enumerationItems,
    associations,
    setAssociations,
}) => {
    const { t } = useTranslation();

    const [filterTerm, setFilterTerm] = useState("");
    const [showOnly, setShowOnly] = useState<FilterChoices>(FilterChoices.ALL);

    return (
        <Grid container>
            <Grid item xs={12}>
                <Typography variant="h5">
                    {t("label.alerts-and-dimensions")}
                </Typography>
                <Typography color="textSecondary" variant="subtitle1">
                    {t(
                        "message.add-individual-alerts-and-dimensions-that-will-send-updates-via-this-subscription-group"
                    )}
                </Typography>
            </Grid>
            <Grid item xs={12}>
                <Card>
                    <CardContent>
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <TextField
                                    placeholder={t(
                                        "label.filter-alerts-by-name"
                                    )}
                                    value={filterTerm}
                                    onChange={(e) =>
                                        setFilterTerm(e.target.value)
                                    }
                                />
                            </Grid>
                            <Grid item>
                                <RadioGroup row value={showOnly}>
                                    <FormControlLabel
                                        control={<Radio color="primary" />}
                                        label={t("label.all")}
                                        labelPlacement="end"
                                        value={FilterChoices.ALL}
                                        onClick={() =>
                                            setShowOnly(FilterChoices.ALL)
                                        }
                                    />
                                    <FormControlLabel
                                        control={<Radio color="primary" />}
                                        label={t("label.basic-alerts-only")}
                                        labelPlacement="end"
                                        value={FilterChoices.BASIC}
                                        onClick={() =>
                                            setShowOnly(FilterChoices.BASIC)
                                        }
                                    />
                                    <FormControlLabel
                                        control={<Radio color="primary" />}
                                        label={t(
                                            "label.dimension-exploration-alerts-only"
                                        )}
                                        labelPlacement="end"
                                        value={FilterChoices.DIM}
                                        onClick={() =>
                                            setShowOnly(FilterChoices.DIM)
                                        }
                                    />
                                </RadioGroup>
                            </Grid>
                        </Grid>
                        <Box pt={2}>
                            <Divider />
                        </Box>
                        <AlertEnumerationItemSelectTable
                            alerts={alerts}
                            associations={associations}
                            enumerationItems={enumerationItems}
                            filterTerm={filterTerm}
                            showOnlyBasic={showOnly === FilterChoices.BASIC}
                            showOnlyDimensionExploration={
                                showOnly === FilterChoices.DIM
                            }
                            onAssociationChange={setAssociations}
                        />
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};
