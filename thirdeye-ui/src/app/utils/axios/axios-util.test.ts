import { getAccessToken, isAuthenticated } from "../auth/auth-util";
import { requestInterceptor } from "./axios-util";

jest.mock("../auth/auth-util");

describe("Axios Util", () => {
    beforeAll(() => {
        (getAccessToken as jest.Mock).mockReturnValue("testToken");
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("requestInterceptor shall attach access token to a request when authenticated", () => {
        (isAuthenticated as jest.Mock).mockReturnValue(true);

        const requestConfig = requestInterceptor({});

        expect(isAuthenticated).toHaveBeenCalled();
        expect(getAccessToken).toHaveBeenCalled();
        expect(requestConfig.headers).toEqual({
            Authorization: "Bearer testToken",
        });
    });

    test("requestInterceptor shall not attach access token to a request when not authenticated", () => {
        (isAuthenticated as jest.Mock).mockReturnValue(false);

        const requestConfig = requestInterceptor({});

        expect(isAuthenticated).toHaveBeenCalled();
        expect(getAccessToken).not.toHaveBeenCalled();
        expect(requestConfig.headers).toBeUndefined();
    });
});
