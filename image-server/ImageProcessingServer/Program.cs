using Microsoft.AspNetCore.Http.HttpResults;
using System.Net;
using System.Runtime;
using ImageMagick;

namespace ImageProcessingServer
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var allowedExtensions = new string[] { ".jpg", ".jpeg", ".png", ".webp", ".bmp" };

            var builder = WebApplication.CreateBuilder(args);
            builder.WebHost.ConfigureKestrel(serverOptions =>
            {
                serverOptions.Listen(System.Net.IPAddress.Any, 8123);
            });


            var app = builder.Build();

            app.MapGet("/", () => "Image processing server is available");

            app.MapPost("/", async (IFormFile file) =>
            {
                if (file == null || file.Length == 0)
                    return Results.BadRequest("No image sent");

                var fileExtension = Path.GetExtension(file.FileName.ToLower());

                if (!allowedExtensions.Contains(fileExtension))
                    return Results.BadRequest("Unsupported file type");

                try
                {
                    var output = await FilterImageAsync(file);

                    return Results.File(output, "image/jpeg");
                } 
                catch (MagickMissingDelegateErrorException)
                {
                    return Results.BadRequest("File extension does not match contents");
                }
                catch (MagickCorruptImageErrorException)
                {
                    return Results.BadRequest("Image contents corrupt or invalid");
                }
                catch (Exception e)
                {
                    return Results.Problem(title: "Unexpected error during file processing", detail: e.Message, statusCode: 500);
                }
            }) .DisableAntiforgery();

            app.Run();
        }

        static async Task<MemoryStream> FilterImageAsync(IFormFile file)
        {
            await using var input = file.OpenReadStream();
            using var image = new MagickImage(input);

            image.ColorSpace = ColorSpace.Gray;

            var output = new MemoryStream();
            await image.WriteAsync(output, MagickFormat.Jpeg);
            output.Position = 0;

            return output;
        }
    }
}
