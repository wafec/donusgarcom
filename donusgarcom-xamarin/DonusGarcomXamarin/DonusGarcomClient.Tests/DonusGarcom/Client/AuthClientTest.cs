using DonusGarcom.Client;
using DonusGarcom.Domain;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using static DonusGarcom.Client.GenericClient;

namespace DonusGarcom.Tests.Client
{
    [TestClass]
    public class AuthClientTest
    {
        Config Configuration { get; set; }

        public AuthClientTest()
        {
            Configuration = new Config
            {
                BaseAddress = "http://localhost:8080/api/"
            };
        }

        [TestMethod]
        public void TestAuthenticateDummyUser()
        {
            AuthClient client = new AuthClient(Configuration);
            AuthDto.AuthUser authUser = new AuthDto.AuthUser
            {
                Username = "dummy",
                Password = "123456"
            };
            AuthDto.AuthToken authToken = client.AuthenticateUser(authUser).GetAwaiter().GetResult();
            Assert.IsNotNull(authToken);
            Assert.IsNotNull(authToken.Token);
        }
    }
}
