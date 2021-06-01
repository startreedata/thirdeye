import axios from "axios";
import { login, logout } from "./auth.rest";

jest.mock("axios");

describe("Auth REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("login should invoke axios.post with appropriate input and return appropriate auth", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAuthResponse,
        });

        await expect(login()).resolves.toEqual(mockAuthResponse);

        expect(axios.post).toHaveBeenCalledWith(
            "/api/auth/login",
            mockAuthRequest
        );
    });

    it("login should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(login()).rejects.toThrow("testError");
    });

    it("logout should invoke axios.post with appropriate input", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({});

        await logout();

        expect(axios.post).toHaveBeenCalledWith("/api/auth/logout");
    });

    it("logout should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(logout()).rejects.toThrow("testError");
    });
});

const mockAuthRequest = new URLSearchParams();
mockAuthRequest.append("grant_type", "password");
mockAuthRequest.append("principal", "admin");
mockAuthRequest.append("password", "password");

const mockAuthResponse = {
    accessToken: "testAccessToken",
};

const mockError = new Error("testError");
