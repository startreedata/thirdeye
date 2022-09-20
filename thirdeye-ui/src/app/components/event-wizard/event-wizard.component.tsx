/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { yupResolver } from "@hookform/resolvers/yup";
import { Box, Button, Grid } from "@material-ui/core";
import { isEmpty, map } from "lodash";
import React, { FunctionComponent, useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../platform/components";
import {
    EditableEvent,
    Event,
    TargetDimensionMap,
} from "../../rest/dto/event.interfaces";
import { createEmptyEvent } from "../../utils/events/events.util";
import { EventMetadataForm } from "./event-metadata-form/event-metadata-form.component";
import { PropertyData } from "./event-metadata-form/event-metadata-form.interfaces";
import { EventPropertiesForm } from "./event-properties-form/event-properties-form.component";
import { EventWizardProps } from "./event-wizard.interface";

const FORM_ID_EVENT_PROPERTIES = "FORM_ID_EVENT_PROPERTIES";

export const EventsWizard: FunctionComponent<EventWizardProps> = ({
    event,
    showCancel,
    onCancel,
    onSubmit,
}) => {
    const { t } = useTranslation();
    const [editedEvent, setEditedEvent] = useState<EditableEvent>(
        event || createEmptyEvent()
    );
    const [propertiesData, setPropertiesData] = useState<PropertyData[]>(() => {
        if (!isEmpty(editedEvent.targetDimensionMap)) {
            return map(editedEvent.targetDimensionMap, (value, key) => {
                return {
                    originalKey: key,
                    propertyName: key,
                    propertyValue: value,
                };
            });
        }

        return [
            {
                originalKey: null,
                propertyName: "",
                propertyValue: [],
            },
        ];
    });

    const { register, handleSubmit, errors, control } = useForm<Event>({
        defaultValues: editedEvent,
        resolver: yupResolver(
            yup.object().shape({
                name: yup.string().required(t("message.event-name-required")),
                type: yup.string(),
                startTime: yup
                    .number()
                    .required(t("message.event-start-time-required")),
                endTime: yup
                    .number()
                    .required(t("message.event-end-time-required")),
            })
        ),
    });

    const onSubmitEventsPropertiesForm = (event: Event): void => {
        setEditedEvent(event);

        const cloned = { ...event };
        const filteredPropertiesData = propertiesData.filter(
            (item) => !!item.propertyName
        );

        delete cloned.targetDimensionMap;

        if (filteredPropertiesData.length > 0) {
            const newTargetDimensionMap: TargetDimensionMap = {};
            filteredPropertiesData.forEach((item) => {
                newTargetDimensionMap[item.propertyName] = item.propertyValue;
            });
            cloned.targetDimensionMap = newTargetDimensionMap;
        }

        onSubmit && onSubmit(cloned);
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid
                    container
                    item
                    noValidate
                    component="form"
                    id={FORM_ID_EVENT_PROPERTIES}
                    xs={12}
                    onSubmit={handleSubmit(onSubmitEventsPropertiesForm)}
                >
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            {/* The useForm functions handles sending data up */}
                            <EventPropertiesForm
                                formControl={control}
                                formErrors={errors}
                                formRegister={register}
                            />
                        </PageContentsCardV1>
                    </Grid>

                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <EventMetadataForm
                                initialPropertiesData={propertiesData}
                                onChange={setPropertiesData}
                            />
                        </PageContentsCardV1>
                    </Grid>
                </Grid>
            </PageContentsGridV1>
            {/* Controls */}
            <Box width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        {showCancel && (
                            <Grid item>
                                <Button color="secondary" onClick={onCancel}>
                                    {t("label.cancel")}
                                </Button>
                            </Grid>
                        )}

                        <Grid item>
                            <Button
                                color="primary"
                                onClick={handleSubmit(
                                    onSubmitEventsPropertiesForm
                                )}
                            >
                                {t("label.create-entity", {
                                    entity: t("label.event"),
                                })}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </>
    );
};
