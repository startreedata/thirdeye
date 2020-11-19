import {
    getAccessToken,
    isAuthenticated,
    removeAccessToken,
    setAccessToken,
} from "./auth-util";

describe("Auth Util", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("isAuthenticated shall return true if an access token is stored in local storage", () => {
        jest.spyOn(Storage.prototype, "getItem").mockReturnValue(
            "testAccessToken"
        );

        const auth = isAuthenticated();

        expect(Storage.prototype.getItem).toHaveBeenCalledWith(
            "LOCAL_STORAGE_KEY_AUTH"
        );
        expect(auth).toBeTruthy();
    });

    test("isAuthenticated shall return false if an access token is not stored in local storage", () => {
        jest.spyOn(Storage.prototype, "getItem").mockReturnValue(null);

        const auth = isAuthenticated();

        expect(Storage.prototype.getItem).toHaveBeenCalledWith(
            "LOCAL_STORAGE_KEY_AUTH"
        );
        expect(auth).toBeFalsy();
    });

    test("getAccessToken shall invoke localStorage.getItem with appropriate input", () => {
        jest.spyOn(Storage.prototype, "getItem").mockReturnValue(
            "testAccessToken"
        );

        const token = getAccessToken();

        expect(Storage.prototype.getItem).toHaveBeenCalledWith(
            "LOCAL_STORAGE_KEY_AUTH"
        );
        expect(token).toEqual("testAccessToken");
    });

    test("setAccessToken shall invoke localStorage.setItem with appropriate input", () => {
        jest.spyOn(Storage.prototype, "setItem").mockImplementation();

        setAccessToken("testAccessToken");

        expect(Storage.prototype.setItem).toHaveBeenCalledWith(
            "LOCAL_STORAGE_KEY_AUTH",
            "testAccessToken"
        );
    });

    test("removeAccessToken shall invoke localStorage.removeItem with appropriate input", () => {
        jest.spyOn(Storage.prototype, "removeItem").mockImplementation();

        removeAccessToken();

        expect(Storage.prototype.removeItem).toHaveBeenCalledWith(
            "LOCAL_STORAGE_KEY_AUTH"
        );
    });
});
