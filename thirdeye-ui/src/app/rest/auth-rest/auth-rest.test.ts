import axios from "axios";
import { Auth } from "../dto/auth.interfaces";
import { login, logout } from "./auth-rest";

jest.mock("axios");

describe("Auth REST", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("login shall invoke axios.post with appropriate input and return result", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({ data: mockAuthResponse });

        const response = await login();

        expect(axios.post).toHaveBeenCalledWith(
            "/api/auth/login",
            mockAuthRequest
        );
        expect(response).toEqual(mockAuthResponse);
    });

    test("login shall throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(login()).rejects.toThrow("testErrorMessage");
    });

    test("logout shall invoke axios.post with appropriate input", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({});

        await logout();

        expect(axios.post).toHaveBeenCalledWith("/api/auth/logout");
    });

    test("logout shall throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(logout()).rejects.toThrow("testErrorMessage");
    });
});

const mockAuthRequest = new URLSearchParams();
mockAuthRequest.append("grant_type", "password");
mockAuthRequest.append("principal", "admin");
mockAuthRequest.append("password", "password");
const mockAuthResponse = {
    accessToken: "testAccessTokenResponse",
} as Auth;
const mockError = new Error("testErrorMessage");
