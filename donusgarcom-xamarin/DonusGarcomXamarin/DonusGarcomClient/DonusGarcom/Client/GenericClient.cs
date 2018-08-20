using System;
using System.Net.Http;
using System.Net.Http.Headers;

namespace DonusGarcom.Client
{
    public class GenericClient
    {
        public Config Configuration { get; set; }
        protected HttpClient Client { get; set; }

        public GenericClient(Config configuration)
        {
            Configuration = configuration;
            Client = CreateClientFromCurrentConfiguration();
        }

        HttpClient CreateClientFromCurrentConfiguration()
        {
            HttpClient client = new HttpClient();
            client.BaseAddress = new Uri(Configuration.BaseAddress);
            client.DefaultRequestHeaders.Accept.Clear();
            client.DefaultRequestHeaders.Accept.Add(
                new MediaTypeWithQualityHeaderValue("application/json"));
            return client;
        }

        public class Config
        {
            public string BaseAddress { get; set; }
            public string Token { get; set; }
            public string Username { get; set; }
            public string Password { get; set; }
        }
    }
}
