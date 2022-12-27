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
    Typography,
    useTheme,
} from "@material-ui/core";
import LeftIcon from "@material-ui/icons/ChevronLeft";
import RightIcon from "@material-ui/icons/ChevronRight";
import CloseIcon from "@material-ui/icons/Close";
import {
    PopoverContentProps,
    ProviderProps,
    TourProvider,
} from "@reactour/tour";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router";
import { DimensionV1 } from "../../utils";
import { getSteps } from "./app-walkthrough-v1.utils";

const TourCard: FunctionComponent<PopoverContentProps> = ({
    steps,
    setCurrentStep,
    currentStep,
    setIsOpen,
}) => {
    const { t } = useTranslation();
    const handleClose = (): void => {
        setIsOpen(false);
    };
    const location = useLocation();

    // TODO: Load tour steps based on location
    console.log({ location });

    const handleBack = (): void => {
        if (currentStep === 0) {
            handleClose();
        } else {
            setCurrentStep((n) => n - 1);
        }
    };
    const handleNext = (): void => {
        if (currentStep >= steps.length - 1) {
            handleClose();
        } else {
            setCurrentStep((n) => n + 1);
        }
    };

    return (
        <Card elevation={0}>
            <CardContent>
                <Box>
                    <Typography variant="body2">
                        {steps[currentStep].content}
                    </Typography>
                    {/* <IconButton
                        color="default"
                        size="small"
                        onClick={() => {
                            onClickClose?.({
                                setIsOpen,
                                setCurrentStep,
                                currentStep,
                            });
                        }}
                    >
                        <CloseIcon fontSize="small" />
                    </IconButton> */}
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

export const AppWalkthroughV1: FunctionComponent = ({ children }) => {
    const theme = useTheme();

    const tourProps = useMemo<Omit<ProviderProps, "children">>(
        () => ({
            scrollSmooth: true,
            onClickMask: (): void => undefined,
            // disableInteraction: true,
            steps: getSteps(),
            ContentComponent: TourCard,
            styles: {
                badge: (base) => ({
                    ...base,
                    background: theme.palette.primary.main,
                }),
                dot: (base, options) => ({
                    ...base,
                    background: options?.current
                        ? theme.palette.primary.main
                        : theme.palette.grey["500"],
                }),
                popover: (base) => ({
                    ...base,
                    padding: "6px 8px",
                    borderRadius: DimensionV1.BorderRadiusDefault,
                    minWidth: 300,
                }),
            },
        }),
        []
    );

    return <TourProvider {...tourProps}>{children}</TourProvider>;
};
