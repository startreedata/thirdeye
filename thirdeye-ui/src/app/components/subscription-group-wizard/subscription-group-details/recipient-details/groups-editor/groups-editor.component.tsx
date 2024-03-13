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
import { Icon } from "@iconify/react";
import { Box, Button, Divider, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationSpec,
    SpecType,
} from "../../../../../rest/dto/subscription-group.interfaces";
import { Email } from "./email/email.component";
import { GroupsEditorProps } from "./groups-editor.interfaces";
import {
    availableSpecTypes,
    generateEmptyEmailSendGridConfiguration,
    specTypeToUIConfig,
} from "./groups-editor.utils";

export const GroupsEditor: FunctionComponent<GroupsEditorProps> = ({
    subscriptionGroup,
    onSubscriptionGroupEmailsChange,
    onSpecsChange,
}) => {
    const [currentSpecs, setCurrentSpecs] = useState<NotificationSpec[]>(
        subscriptionGroup.specs || []
    );
    const [legacyEmailList, setLegacyEmailList] = useState<string[]>(
        subscriptionGroup.notificationSchemes?.email.to || []
    );
    const { t } = useTranslation();

    useEffect(() => {
        onSpecsChange(currentSpecs);
    }, [currentSpecs]);

    const hasSomeConfig =
        (subscriptionGroup.notificationSchemes?.email &&
            subscriptionGroup.notificationSchemes?.email.to.length > 0) ||
        currentSpecs.length > 0;

    const handleShortcutCreateOnclick = (id: number | string): void => {
        switch (id) {
            case SpecType.EmailSendgrid:
                setCurrentSpecs((specs) => {
                    return [
                        ...specs,
                        generateEmptyEmailSendGridConfiguration(),
                    ];
                });

                break;
            case SpecType.Slack:
                setCurrentSpecs((specs) => {
                    return [
                        ...specs,
                        {
                            type: SpecType.Slack,
                            params: {
                                webhookUrl: "",
                                notifyResolvedAnomalies: false,
                            },
                        },
                    ];
                });

                break;

            case SpecType.Webhook:
                setCurrentSpecs((specs) => {
                    return [
                        ...specs,
                        {
                            type: SpecType.Webhook,
                            params: {
                                url: "",
                            },
                        },
                    ];
                });

                break;

            case SpecType.PagerDuty:
                setCurrentSpecs((specs) => {
                    return [
                        ...specs,
                        {
                            type: SpecType.PagerDuty,
                            params: {
                                eventsIntegrationKey: "",
                            },
                        },
                    ];
                });

                break;
            default:
                break;
        }
    };

    const handleSpecChange = (
        updatedSpec: NotificationSpec,
        idx: number
    ): void => {
        setCurrentSpecs((current) => {
            return current.map((spec, specIdx) => {
                if (specIdx === idx) {
                    return {
                        ...spec,
                        ...updatedSpec,
                    };
                }

                return spec;
            });
        });
    };

    const handleSpecDelete = (idx: number): void => {
        setCurrentSpecs((current) => {
            return current.filter((_, specIdx) => {
                return specIdx !== idx;
            });
        });
    };

    const handleEmailDelete = (): void => {
        setLegacyEmailList([]);
        onSubscriptionGroupEmailsChange([]);
    };

    return (
        <>
            {/** Legacy Email Configuration */}
            {hasSomeConfig && legacyEmailList.length > 0 && (
                <Box marginBottom={3}>
                    <Email
                        subscriptionGroup={subscriptionGroup}
                        onDeleteClick={handleEmailDelete}
                        onSubscriptionGroupEmailsChange={
                            onSubscriptionGroupEmailsChange
                        }
                    />
                </Box>
            )}

            {hasSomeConfig &&
                currentSpecs.map((spec: NotificationSpec, idx: number) => {
                    if (specTypeToUIConfig[spec.type]) {
                        return (
                            <Box
                                key={`${spec.type}-${idx}`}
                                paddingBottom={1}
                                paddingTop={1}
                            >
                                {React.createElement(
                                    specTypeToUIConfig[spec.type].formComponent,
                                    {
                                        configuration: spec,
                                        onDeleteClick: () =>
                                            handleSpecDelete(idx),
                                        onSpecChange: (
                                            updatedSpec: NotificationSpec
                                        ) => handleSpecChange(updatedSpec, idx),
                                    }
                                )}
                                <Divider />
                            </Box>
                        );
                    }

                    return null;
                })}

            <Box paddingBottom={1} paddingTop={1}>
                <Grid container>
                    {availableSpecTypes.map((item) => (
                        <Grid item key={item.id}>
                            <Button
                                color="primary"
                                data-testid={item.id}
                                variant="outlined"
                                onClick={() =>
                                    handleShortcutCreateOnclick(item.id)
                                }
                            >
                                <Box display="block" minWidth={100}>
                                    <Box color="primary" textAlign="center">
                                        <Icon height={48} icon={item.icon} />
                                    </Box>

                                    <Box
                                        color="primary"
                                        marginTop={2}
                                        textAlign="center"
                                    >
                                        {t("label.add-entity", {
                                            entity: t(
                                                item.internationalizationString
                                            ),
                                        })}
                                    </Box>
                                </Box>
                            </Button>
                        </Grid>
                    ))}
                </Grid>
            </Box>
        </>
    );
};
