using Amazon;
using Amazon.S3;
using ImageProcessingServer.Service;

namespace ImageProcessingServer
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.Services.AddControllers();
            builder.Services.AddHealthChecks();
            builder.Services.AddSingleton<IAmazonS3>(
                _ => new AmazonS3Client(RegionEndpoint.EUCentral1)
            );
            builder.Services.AddScoped<IImageStorage, S3ImageStorage>();
            builder.Services.AddScoped<IImageProcessingService, ImageProcessingService>();

            builder.WebHost.ConfigureKestrel(serverOptions =>
            {
                serverOptions.Listen(System.Net.IPAddress.Any, 8123);
            });

            var app = builder.Build();

            app.UseAuthorization();
            app.MapControllers();
            app.MapHealthChecks("/health/live");
            app.MapHealthChecks("/health/ready");

            app.Run();
        }
    }
}
