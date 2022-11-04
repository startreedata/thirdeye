import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { AlertTemplatePropertiesBuilder } from "./alert-template-properties-builder.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("AlertTemplatePropertiesBuilder", () => {
    it("should initially render just the required fields", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertTemplatePropertiesBuilder
                alertTemplateId={1}
                defaultTemplateProperties={{
                    optionalField: "fooBar",
                }}
                requiredFields={["requiredField", "optionalField"]}
                templateProperties={{
                    requiredField: "helloWorld",
                }}
                onPropertyValueChange={mockCallback}
            />
        );

        expect(
            screen.getByTestId("textfield-requiredField")
        ).toBeInTheDocument();

        expect(screen.queryByTestId("textfield-optionalField")).toBeNull();
        expect(screen.getByTestId("show-more-btn")).toBeInTheDocument();
    });

    it("should render optional fields after show more button is clicked", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertTemplatePropertiesBuilder
                alertTemplateId={1}
                defaultTemplateProperties={{
                    optionalField: "fooBar",
                }}
                requiredFields={["requiredField", "optionalField"]}
                templateProperties={{
                    requiredField: "helloWorld",
                }}
                onPropertyValueChange={mockCallback}
            />
        );

        fireEvent.click(screen.getByTestId("show-more-btn"));

        const optionalField = await screen.findByTestId(
            "textfield-optionalField"
        );

        expect(optionalField).toBeInTheDocument();
        expect(
            screen.getByTestId("textfield-requiredField")
        ).toBeInTheDocument();
        expect(screen.getByTestId("hide-more-btn")).toBeInTheDocument();
    });
});
