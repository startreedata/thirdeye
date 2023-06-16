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
import { Button, ButtonGroup } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Link, useLocation } from "react-router-dom";
import { AppRouteRelative } from "../../../utils/routes/routes.util";

export const InvestigationStepsNavigation: FunctionComponent = () => {
    const { t } = useTranslation();
    const location = useLocation();

    const stepItems = [
        {
            matcher: (path: string) =>
                path.includes(AppRouteRelative.RCA_WHAT_WHERE),
            navLink: `${AppRouteRelative.RCA_WHAT_WHERE}`,
            text: t("label.1-what-went-wrong-and-where"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_EVENTS),
            navLink: AppRouteRelative.RCA_EVENTS,
            text: t("label.2-an-event-could-have-caused-it"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_REVIEW_SHARE),
            navLink: AppRouteRelative.RCA_REVIEW_SHARE,
            text: t("label.3-review-investigation-share"),
        },
    ];

    const currentPage = useMemo(() => {
        return stepItems.find((candidate) => {
            return candidate.matcher(location.pathname);
        });
    }, [location]);

    return (
        <ButtonGroup fullWidth variant="outlined">
            {stepItems.map((btnConfig) => {
                return (
                    <Button
                        color={
                            currentPage?.navLink === btnConfig.navLink
                                ? "primary"
                                : undefined
                        }
                        component={Link}
                        key={btnConfig.text}
                        to={btnConfig.navLink}
                    >
                        {btnConfig.text}
                    </Button>
                );
            })}
        </ButtonGroup>
    );
};
