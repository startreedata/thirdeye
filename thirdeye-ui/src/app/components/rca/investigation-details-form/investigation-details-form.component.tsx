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
import { Grid, TextField } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { InvestigationDetailsFormProps } from "./investigation-details-form.interfaces";

export const InvestigationDetailsForm: FunctionComponent<InvestigationDetailsFormProps> =
    ({ onCommentChange, onNameChange, investigation }) => {
        const { t } = useTranslation();

        const [name, setName] = useState<string>(investigation.name);
        const [comment, setComment] = useState<string>(investigation.text);

        return (
            <Grid container>
                <InputSection
                    fullWidth
                    inputComponent={
                        <TextField
                            fullWidth
                            placeholder={t("message.name-your-investigation")}
                            value={name}
                            variant="outlined"
                            onChange={(e) => {
                                setName(e.target.value);
                                onNameChange(e.target.value);
                            }}
                        />
                    }
                    label={t("label.investigation-name")}
                />

                <InputSection
                    fullWidth
                    helperLabel={`(${t("label.optional")})`}
                    inputComponent={
                        <TextField
                            fullWidth
                            multiline
                            minRows={3}
                            placeholder={t(
                                "message.add-comments-or-conclusion"
                            )}
                            value={comment}
                            variant="outlined"
                            onChange={(e) => {
                                setComment(e.target.value);
                                onCommentChange(e.target.value);
                            }}
                        />
                    }
                    label={t("label.comments")}
                />
            </Grid>
        );
    };
