import numbro from "numbro";
import { enUS } from "../../locale/numbers/en-us";
import { registerLanguages } from "./numbro.util";

describe("numbro Util", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("registerLanguages should register appropriate languages", () => {
        jest.spyOn(numbro, "registerLanguage").mockImplementation();
        registerLanguages();

        expect(numbro.registerLanguage).toHaveBeenCalledWith(enUS);
    });
});
