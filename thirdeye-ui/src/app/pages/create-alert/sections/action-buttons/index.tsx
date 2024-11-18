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
import React, { useState } from "react";
import { Box, Button, Grid } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

// utils
import { getAlertsAllPath } from "../../../../utils/routes/routes.util";

// state
import { useCreateAlertStore } from "../../hooks/state";

// section
import { ConfirmationModal } from "./confirmation-modal";

export const CreateActionButtons = (): JSX.Element => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { workingAlert, selectedDetectionAlgorithm, alertInsight } =
        useCreateAlertStore();

    const [openCreateAlertModal, setOpenCreateAlertModal] = useState(false);

    return (
        <>
            <Grid item xs={12}>
                <Box display="flex" gridGap={10} paddingTop={2}>
                    <Button
                        color="primary"
                        variant="outlined"
                        onClick={() => {
                            navigate(getAlertsAllPath());
                        }}
                    >
                        {t("label.cancel")}
                    </Button>
                    <Button
                        color="primary"
                        disabled={!selectedDetectionAlgorithm}
                        onClick={() => setOpenCreateAlertModal(true)}
                    >
                        {t(
                            workingAlert.id
                                ? "label.update-alert"
                                : "label.create-alert"
                        )}
                    </Button>
                </Box>
            </Grid>
            {openCreateAlertModal && (
                <ConfirmationModal
                    defaultCron={alertInsight?.defaultCron}
                    onCancel={() => setOpenCreateAlertModal(false)}
                />
            )}
        </>
    );
};
