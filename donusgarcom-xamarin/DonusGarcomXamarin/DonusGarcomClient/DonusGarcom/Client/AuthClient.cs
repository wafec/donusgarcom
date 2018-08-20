using DonusGarcom.Domain;
using System.Net.Http;
using System.Threading.Tasks;

namespace DonusGarcom.Client
{
    public class AuthClient : GenericClient
    {
        public AuthClient(Config configuration) : base(configuration) { }

        public async Task<AuthDto.AuthToken> AuthenticateUser(AuthDto.AuthUser authUser)
        {
            AuthDto.AuthToken authToken = null;
            HttpResponseMessage response = await Client.PostAsJsonAsync("auth/token", authUser);
            if (response.IsSuccessStatusCode)
            {
                authToken = await response.Content.ReadAsAsync<AuthDto.AuthToken>();
            }
            return authToken;
        }
    }
}
