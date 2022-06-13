import { createDefaultAlertTemplate } from "./alert-templates.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Alert Templates Util", () => {
    it("createDefaultAlertTemplate should contain expected fields", () => {
        const result = createDefaultAlertTemplate();

        expect(result.name).not.toBeNull();
        expect(result.description).not.toBeNull();
        expect(result.cron).not.toBeNull();
        expect(result.nodes).not.toBeNull();
        expect(result.metadata).not.toBeNull();
    });
});
