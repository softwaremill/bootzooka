export interface UserRegistrationData {
  login: string;
  email: string;
  password: string;
}

export interface ChangePasswordData {
  currentPassword: string;
  newPassword: string;
}

export interface LoginData {
  loginOrEmail: string;
  password: string;
}

export interface User {
  email: string
  login: string
}

export interface Version {
  buildDate: string;
  buildSha: string;
}
