import numbro from "numbro";
import { enUS } from "../../locale/numbers/en-us";

// Registers numbro languages
export const registerLanguages = (): void => {
    numbro.registerLanguage(enUS);
};
