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
import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { MetadataProperty } from "../../../../rest/dto/alert-template.interfaces";
import { AlertTemplateFormField } from "./alert-template-form-field.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../../../platform/components", () => ({
    JSONEditorV1: jest.fn().mockImplementation((props) => <div {...props} />),
}));

describe("AlertTemplateFormField", () => {
    it("string field should render text box", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertTemplateFormField
                item={STRING_FIELD}
                placeholder=""
                tabIndex={0}
                onChange={mockCallback}
            />
        );

        expect(screen.getByTestId("textfield-stringField")).toBeInTheDocument();

        fireEvent.change(screen.getByTestId("input-stringField"), {
            target: { value: "newValue" },
        });

        expect(mockCallback).toHaveBeenCalledWith("newValue");
    });

    it("boolean field should render switch", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertTemplateFormField
                item={BOOLEAN_FIELD}
                placeholder=""
                tabIndex={0}
                onChange={mockCallback}
            />
        );

        expect(screen.getByTestId("switch-booleanField")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("checkbox"));

        expect(mockCallback).toHaveBeenCalledWith(true);

        fireEvent.click(screen.getByRole("checkbox"));

        expect(mockCallback).toHaveBeenCalledWith(false);
    });

    it("array field should render json editor", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertTemplateFormField
                item={OBJECT_FIELD}
                placeholder=""
                tabIndex={0}
                onChange={mockCallback}
            />
        );

        expect(
            screen.getByTestId("jsoneditor-objectField")
        ).toBeInTheDocument();
    });

    it("enumerationItems field should render json editor", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertTemplateFormField
                item={ENUMERATION_ITEM_FIELD}
                placeholder=""
                tabIndex={0}
                onChange={mockCallback}
            />
        );

        expect(
            screen.getByTestId("jsoneditor-enumerationItems")
        ).toBeInTheDocument();
    });
});

const STRING_FIELD = {
    key: "stringField",
    value: "",
    metadata: {
        name: "stringField",
        defaultValue: "fooBar",
        defaultIsNull: false,
        multiselect: false,
    },
};

const BOOLEAN_FIELD = {
    key: "booleanField",
    value: false,
    metadata: {
        name: "booleanField",
        defaultIsNull: false,
        multiselect: false,
        jsonType: "BOOLEAN" as MetadataProperty["jsonType"],
    },
};

const OBJECT_FIELD = {
    key: "objectField",
    value: {},
    metadata: {
        name: "objectField",
        defaultIsNull: false,
        multiselect: false,
        jsonType: "OBJECT" as MetadataProperty["jsonType"],
    },
};

const ENUMERATION_ITEM_FIELD = {
    key: "enumerationItems",
    value: {},
    metadata: {
        name: "enumerationItems",
        defaultIsNull: false,
        multiselect: false,
    },
};
