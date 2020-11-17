import axios from "axios";
import { Auth } from "../dto/auth.interfaces";
import { login } from "./auth.rest";

jest.mock("axios");

const mockAuthRequest = new URLSearchParams();
mockAuthRequest.append("grant_type", "password");
mockAuthRequest.append("principal", "admin");
mockAuthRequest.append("password", "password");

const mockAuthResponse: Auth = {
    accessToken: "testAccessTokenResponse",
} as Auth;

const mockError = {
    message: "testError",
};

describe("Auth REST", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("login shall invoke axios.post with appropriate input and return result", async () => {
        (axios.post as jest.Mock).mockResolvedValue({ data: mockAuthResponse });

        const response = await login();

        expect(axios.post).toHaveBeenCalledWith(
            "/api/auth/login",
            mockAuthRequest
        );
        expect(response).toEqual(mockAuthResponse);
    });

    test("login shall throw encountered error", async () => {
        (axios.post as jest.Mock).mockRejectedValue(mockError);

        try {
            await login();
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });
});
