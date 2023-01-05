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

import {
    Box,
    Button,
    Card,
    CardActions,
    CardContent,
    IconButton,
    Typography,
} from "@material-ui/core";
import LeftIcon from "@material-ui/icons/ChevronLeft";
import RightIcon from "@material-ui/icons/ChevronRight";
import CloseIcon from "@material-ui/icons/Close";
import { PopoverContentProps } from "@reactour/tour";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ExtendedStepType } from "../app-walkthrough-v1.utils";

export const TourCard: FunctionComponent<
    Omit<PopoverContentProps, "steps"> & { steps: ExtendedStepType[] }
> = ({ steps, setCurrentStep, currentStep, setIsOpen }) => {
    const { t } = useTranslation();
    const handleClose = (): void => {
        setIsOpen(false);
        setCurrentStep(0);
    };
    // TODO: Load based on location

    const currentStepData = steps?.[currentStep];

    const handleBack = (): void => {
        if (currentStep === 0) {
            handleClose();
        } else {
            setCurrentStep((n) => n - 1);
        }
    };
    const handleNext = (): void => {
        if (currentStepData.disableNext) {
            return;
        }

        if (currentStep >= steps.length - 1) {
            handleClose();
        } else {
            setCurrentStep((n) => n + 1);
        }
    };

    return (
        <Card elevation={0}>
            <CardContent>
                <Box
                    alignItems="center"
                    display="flex"
                    justifyContent="space-between"
                >
                    <Typography variant="body2">
                        {steps[currentStep].content}
                    </Typography>
                    <IconButton
                        color="secondary"
                        size="small"
                        onClick={() => {
                            handleClose();
                        }}
                    >
                        <CloseIcon fontSize="small" />
                    </IconButton>
                </Box>
            </CardContent>
            <CardActions>
                <Box
                    alignItems="center"
                    display="flex"
                    justifyContent="space-between"
                    width="100%"
                >
                    <Button
                        size="small"
                        startIcon={
                            currentStep === 0 ? (
                                <CloseIcon fontSize="small" />
                            ) : (
                                <LeftIcon fontSize="small" />
                            )
                        }
                        variant="text"
                        onClick={handleBack}
                    >
                        {currentStep === 0 ? t("label.close") : t("label.back")}
                    </Button>
                    <Typography color="textSecondary" variant="caption">
                        {currentStep + 1} of {steps.length}
                    </Typography>
                    <Button
                        color="primary"
                        disabled={currentStepData?.disableNext}
                        endIcon={
                            currentStep === steps.length - 1 ? (
                                <CloseIcon fontSize="small" />
                            ) : (
                                <RightIcon fontSize="small" />
                            )
                        }
                        size="small"
                        variant="text"
                        onClick={handleNext}
                    >
                        {currentStep === steps.length - 1
                            ? "End Tour"
                            : t("label.next")}
                    </Button>
                </Box>
            </CardActions>
        </Card>
    );
};
