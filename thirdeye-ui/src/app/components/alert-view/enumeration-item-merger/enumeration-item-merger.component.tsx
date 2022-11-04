import { Card, CardContent } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { useGetEnumerationItems } from "../../../rest/enumeration-items/enumeration-items.actions";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import {
    DetectionEvaluationForRender,
    EnumerationItemMergerProps,
} from "./enumeration-item-merger.interfaces";
import { mergeContent } from "./enumeration-item-merger.util";

export const EnumerationItemMerger: FunctionComponent<EnumerationItemMergerProps> =
    ({ anomalies, detectionEvaluations, children }) => {
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();
        const { enumerationItems, getEnumerationItems, status, errorMessages } =
            useGetEnumerationItems();
        const [mergedContent, setMergedContent] =
            useState<DetectionEvaluationForRender[]>();

        useEffect(() => {
            setMergedContent(undefined);
            const enumerationItemIds = new Set<number>();

            anomalies.forEach((anomaly) => {
                if (anomaly.enumerationItem) {
                    enumerationItemIds.add(anomaly.enumerationItem.id);
                }
            });

            if (enumerationItemIds.size > 0) {
                getEnumerationItems({ ids: Array.from(enumerationItemIds) });
            } else {
                setMergedContent(
                    mergeContent(anomalies, detectionEvaluations, [])
                );
            }
        }, [anomalies]);

        useEffect(() => {
            if (enumerationItems) {
                setMergedContent(
                    mergeContent(
                        anomalies,
                        detectionEvaluations,
                        enumerationItems
                    )
                );
            }
        }, [enumerationItems]);

        useEffect(() => {
            notifyIfErrors(
                status,
                errorMessages,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.alert"),
                })
            );
        }, [status]);

        if (mergedContent === undefined) {
            return (
                <Card variant="outlined">
                    <CardContent>
                        <SkeletonV1 />
                        <SkeletonV1 />
                        <SkeletonV1 />
                        <SkeletonV1 />
                    </CardContent>
                </Card>
            );
        }

        return children(mergedContent);
    };
