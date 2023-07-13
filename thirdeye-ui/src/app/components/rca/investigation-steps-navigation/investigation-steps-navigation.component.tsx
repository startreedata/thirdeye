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
import { Button, Step, StepLabel, Stepper } from "@material-ui/core";
import AdjustIcon from "@material-ui/icons/Adjust";
import FiberManualRecordIcon from "@material-ui/icons/FiberManualRecord";
import FiberManualRecordOutlinedIcon from "@material-ui/icons/FiberManualRecordOutlined";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Link, useLocation, useSearchParams } from "react-router-dom";
import { AppRouteRelative } from "../../../utils/routes/routes.util";

const CircleIcon = ({
    active,
    completed,
}: {
    active: boolean;
    completed: boolean;
}): React.ReactNode => {
    if (active) {
        return <AdjustIcon color="primary" />;
    }

    return completed ? (
        <FiberManualRecordIcon color="primary" />
    ) : (
        <FiberManualRecordOutlinedIcon color="primary" />
    );
};

export const InvestigationStepsNavigation: FunctionComponent = () => {
    const { t } = useTranslation();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const stepItems = [
        {
            matcher: (path: string) =>
                path.includes(AppRouteRelative.RCA_WHAT_WHERE),
            navLink: `${AppRouteRelative.RCA_WHAT_WHERE}`,
            text: t("label.what-went-wrong-and-where"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_EVENTS),
            navLink: AppRouteRelative.RCA_EVENTS,
            text: t("label.an-event-could-have-caused-it"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_REVIEW_SHARE),
            navLink: AppRouteRelative.RCA_REVIEW_SHARE,
            text: t("label.review-investigation-share"),
        },
    ];

    const currentPageIdx = useMemo(() => {
        return stepItems.findIndex((candidate) => {
            return candidate.matcher(location.pathname);
        });
    }, [location]);

    return (
        <Stepper activeStep={currentPageIdx}>
            {stepItems.map((stepConfig) => {
                return (
                    <Step key={stepConfig.navLink}>
                        <StepLabel
                            StepIconComponent={CircleIcon as React.ElementType}
                        >
                            <Button
                                color="default"
                                component={Link}
                                to={`${
                                    stepConfig.navLink
                                }?${searchParams.toString()}`}
                                variant="text"
                            >
                                {stepConfig.text}
                            </Button>
                        </StepLabel>
                    </Step>
                );
            })}
        </Stepper>
    );
};
