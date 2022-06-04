import { EditableEvent } from "../../rest/dto/event.interfaces";
import { generateDateRangeMonthsFromNow } from "../routes/routes.util";

export const createEmptyEvent = (): EditableEvent => {
    const [start, end] = generateDateRangeMonthsFromNow(0);

    return {
        name: "",
        startTime: start,
        endTime: end,
        type: "",
    };
};
