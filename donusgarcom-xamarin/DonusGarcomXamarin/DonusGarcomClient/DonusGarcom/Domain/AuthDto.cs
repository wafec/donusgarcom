namespace DonusGarcom.Domain
{
    public class AuthDto
    {
        public class AuthToken
        {
            public string Token { get; set; }
        }

        public class AuthUser
        {
            public string Username { get; set; }
            public string Password { get; set; }
        }
    }
}
