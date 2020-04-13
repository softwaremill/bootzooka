import { get } from '../Api/api';
import { Version } from "../types/Types";

export const getAppVersion = async () => await get<Version>("/admin/version");
