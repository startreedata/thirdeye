import axios from "axios";
import { Auth } from "../dto/auth.interfaces";
import { login, logout } from "./auth-rest";

jest.mock("axios");

describe("Auth REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("login should invoke axios.post with appropriate input and return appropriate auth", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAuthResponse,
        });

        expect(await login()).toEqual(mockAuthResponse);
        expect(axios.post).toHaveBeenCalledWith(
            "/api/auth/login",
            mockAuthRequest
        );
    });

    test("login should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(login()).rejects.toThrow("testErrorMessage");
    });

    test("logout should invoke axios.post with appropriate input", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({});

        await logout();

        expect(axios.post).toHaveBeenCalledWith("/api/auth/logout");
    });

    test("logout should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(logout()).rejects.toThrow("testErrorMessage");
    });
});

const mockAuthRequest = new URLSearchParams();
mockAuthRequest.append("grant_type", "password");
mockAuthRequest.append("principal", "admin");
mockAuthRequest.append("password", "password");

const mockAuthResponse: Auth = {
    accessToken: "testAccessTokenResponse",
} as Auth;

const mockError = new Error("testErrorMessage");
