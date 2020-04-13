import axios from 'axios';
import { Either } from "ts-matches";
import { post } from "../Services/api";

class PasswordService {
  static context = 'api/v1/passwordreset';

  // TODO extract to a separate service, add unit tests
  static claimPasswordRest(loginOrEmail: string): Promise<Either<Error, string>> {
    return post(`${PasswordService.context}/forgot`, { loginOrEmail });
  }
  static resetPassword(code: string, password: string) {
    return axios.post(`${PasswordService.context}/reset`, { code, password });
  }
}

export default PasswordService;
