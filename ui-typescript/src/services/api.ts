import axios, { AxiosError, AxiosRequestConfig } from "axios";
import { Either, Left, Right } from "ts-matches";

class Api {
  constructor(readonly apiUrl: string) {
  }

  public async get<T, Err = any>(url: string, config: AxiosRequestConfig = {}): Promise<Either<AxiosError<Err>, T>> {
    try {
      const { data } = await axios.get(url, config);
      return Right.of(data);
    } catch (error) {
      return Left.of(error)
    }
  }

  public async post<V, T, Err = any>(url: string, data: V, config: AxiosRequestConfig): Promise<Either<AxiosError<Err>, T>> {
    try {
      const { response } = await axios.post(url, data, config);
      return Right.of(response);
    } catch (error) {
      return Left.of(error);
    }
  }
}
