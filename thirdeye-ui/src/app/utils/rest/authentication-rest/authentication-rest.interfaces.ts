export interface User {
    id: number;
    principal: string;
    created: string;
}

export interface Authentication {
    user: User;
    accessToken: string;
}
