import { NotificationTypeV1 } from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { notifyIfErrors } from "./notifications.util";

describe("Notifications Util", () => {
    it("notifyIfErrors should not call notify if request status is not Error", () => {
        const mockNotify = jest.fn();
        notifyIfErrors(ActionStatus.Initial, ["bar"], mockNotify, "foo");

        expect(mockNotify).toHaveBeenCalledTimes(0);
    });

    it("notifyIfErrors should call notify if request status is Error and errorMessages is not empty", () => {
        const mockNotify = jest.fn();
        notifyIfErrors(
            ActionStatus.Error,
            ["bar", "hello", "world"],
            mockNotify,
            "foo"
        );

        expect(mockNotify).toHaveBeenCalledTimes(3);
    });

    it("notifyIfErrors should call notify if with fallback msg", () => {
        const mockNotify = jest.fn();
        notifyIfErrors(ActionStatus.Error, null, mockNotify, "foo");

        expect(mockNotify).toHaveBeenCalledWith(
            NotificationTypeV1.Error,
            "foo"
        );
    });
});
