import axios from "axios";
import versionService from "./VersionService";

jest.mock("axios");

test("fetches version from API", async () => {
  const data = { buildDate: "testDate", buildSha: "testSha" };

  (axios.get as jest.Mock).mockResolvedValueOnce({ data });

  await expect(versionService.getVersion()).resolves.toEqual(data);
  expect(axios.get).toBeCalledWith("admin/version");
});
