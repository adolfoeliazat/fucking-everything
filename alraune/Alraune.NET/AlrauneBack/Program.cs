using System;
using System.Collections.Immutable;
using System.IO;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;

namespace AlrauneBack
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var dir = Directory.GetCurrentDirectory();
            Console.WriteLine("dir = " + dir);
            var builder = new WebHostBuilder()
                .UseContentRoot(dir)
                .UseWebRoot("static")
                .UseStartup<Startup>()
                .UseKestrel()
                .UseUrls("http://localhost:5000");

            var host = builder.Build();
            host.Run();

            Console.WriteLine("Pizda");
        }
    }

    public class Startup
    {
        public void ConfigureServices(IServiceCollection services)
        {
        }

        public void Configure(IApplicationBuilder app)
        {
            app.UseStaticFiles();
        }
    }
}












