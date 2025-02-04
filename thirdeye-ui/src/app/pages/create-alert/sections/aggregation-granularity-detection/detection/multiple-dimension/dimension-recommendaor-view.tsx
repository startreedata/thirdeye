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
// external
import React from "react";
import { Box, Button, Grid, Typography } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { AddCircleOutline } from "@material-ui/icons";

// styles
import { multipleDimensionStyle } from "./styles";

// assets
import DimensionImage from "../../../../../../../assets/images/dimensions.png";

// state
import { useCreateAlertStore } from "../../../../hooks/state";

export const DimensionRecommendorView = (): JSX.Element => {
    const { t } = useTranslation();
    const componentStyles = multipleDimensionStyle();
    const { setShowDimensionRecommendorModal } = useCreateAlertStore();

    return (
        <>
            <Grid item xs={12}>
                <Box
                    className={componentStyles.card}
                    display="flex"
                    justifyContent="center"
                >
                    <Grid>
                        <Box marginBottom="10px">
                            <Typography variant="h5">
                                {t("label.dimensions-recommender")}
                            </Typography>
                            <Typography variant="body2">
                                {t(
                                    "message.find-top-dimension-contributors-to-create-the-alert"
                                )}
                            </Typography>
                        </Box>
                        <Button
                            color="primary"
                            startIcon={<AddCircleOutline />}
                            variant="outlined"
                            onClick={() =>
                                setShowDimensionRecommendorModal(true)
                            }
                        >
                            {t("label.add-dimensions")}
                        </Button>
                    </Grid>
                    <Grid>
                        <img alt="Dimension recommender" src={DimensionImage} />
                    </Grid>
                </Box>
            </Grid>
        </>
    );
};
