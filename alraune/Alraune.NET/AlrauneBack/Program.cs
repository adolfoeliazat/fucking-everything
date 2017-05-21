using System;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices.ComTypes;
using System.Text;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using static AlrauneBack.Pile;

namespace AlrauneBack {
    public class Program {
        public static void Main(string[] args) {
            var dir = Directory.GetCurrentDirectory();
            Console.WriteLine("dir = " + dir);
            var builder = new WebHostBuilder()
                .UseContentRoot(dir)
                .UseWebRoot("")
                .UseStartup<Startup>()
                .UseKestrel()
                .UseUrls("http://localhost:5000");

            var host = builder.Build();
            host.Run();
        }
    }

    public class Startup {
        public void ConfigureServices(IServiceCollection services) {
        }

        public void Configure(IApplicationBuilder app, ILoggerFactory loggerFactory) {
            loggerFactory.AddConsole();
            app.UseStaticFiles();
            app.Run(async context => {
                Pile.Log = context.RequestServices.GetService<ILogger<Program>>();
                var path = context.Request.Path.ToString();
                var pageName = path.Substring(1).Capitalize();
                var methodName = $"Serve{pageName}Page";
                Log.LogInformation($"{nameof(methodName)} = {methodName}");
                var method = GetType().GetMethod(methodName);
                if (method == null)
                    method = GetType().GetMethod(nameof(ServeLandingPage));
                context.Response.ContentType = "text/html; charset=utf-8";
                await context.Response.WriteAsync(method.Invoke(this, new object[]{}) as string);
            });
        }

        public string ServeFuckPage() => "I am the fuck page";

        public string ServeLandingPage() => "I am the landing page";

        public interface IRenderable {
            string Render();
        }

//        public class Text(string s) : IRenderable {
//            public string Render() {
//                throw new NotImplementedException();
//            }
//        }
//
//        public class HTMLBuilder {
//            public HTMLBuilder Add(IRenderable re) {
//                return this;
//            }
//        }

        public string ServeOrderPage() => new StringBuilder()
            .Ln("<!DOCTYPE html>")
            .Ln("<html lang='en'>")
            .Ln("<head>")
            .Ln("    <meta charset='utf-8'>")
            .Ln("    <meta http-equiv='X-UA-Compatible' content='IE=edge'>")
            .Ln("    <meta name='viewport' content='width=device-width, initial-scale=1'>")
            .Ln("    <title>Alraune</title>")
            .Ln("")
            .Ln("    <link href='node_modules/bootstrap/dist/css/bootstrap.min.css' rel='stylesheet'>")
            .Ln("    <link rel='stylesheet. href='node_modules/font-awesome/css/font-awesome.min.css'>")
            .Ln("</head>")
            .Ln("<body>")
            .Ln($"    {T("TOTE", "Заказ")}")
            .Ln("")
            .Ln("    <script src='node_modules/jquery/dist/jquery.min.js'></script>")
            .Ln("    <script src='node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
            .Ln("</body>")
            .Ln("</html>")
            .ToString();
    }
}