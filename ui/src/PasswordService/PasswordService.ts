import { Either } from "ts-matches";
import { post } from "../Api/api";

class PasswordService {
  static context = 'api/v1/passwordreset';

  // TODO extract to a separate service, add unit tests
  static claimPasswordReset(loginOrEmail: string): Promise<Either<Error, string>> {
    return post(`${PasswordService.context}/forgot`, { loginOrEmail });
  }
  static resetPassword(code: string, password: string) {
    return post(`${PasswordService.context}/reset`, { code, password });
  }
}

export default PasswordService;
