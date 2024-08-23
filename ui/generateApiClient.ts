const OpenAPIClientAxios = require('openapi-client-axios').default;

const generateApiClient = async () => {

  console.log('nannaa  btaman')
  const apiSpecPath = 'http://localhost:3000/openapi.yaml';


  const api = new OpenAPIClientAxios({
    definition: apiSpecPath,
    axiosConfigDefaults: {
      baseURL: 'http://localhost:8080/api/v1/',
    },
  });

  await api.init();

  async function createUser() {
    const client = await api.getClient();
    const data = {
      "login": "3test",
      "email": "3test@test.com",
      "password": "pass"
    }
    const res = await client.postUserRegister(null, data);
    console.log("User created", res.data);
  }

  await createUser()
};

module.exports = generateApiClient;




