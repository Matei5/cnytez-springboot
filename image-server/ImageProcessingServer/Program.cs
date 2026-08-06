using Microsoft.AspNetCore.Http.HttpResults;
using System.Net;
using System.Runtime;
using ImageMagick;
using Amazon;
using Amazon.S3.Model;
using Amazon.Runtime.Internal.Auth;
using Amazon.S3;

namespace ImageProcessingServer
{
    public class Program
    {
        private const string bucketName = "cnytez-image-server-s3";
        private static readonly RegionEndpoint bucketRegion = RegionEndpoint.EUCentral1;
        private static AmazonS3Client s3Client = new AmazonS3Client(bucketRegion);
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

            app.MapPost("/{filter?}", async (IFormFile file, string? filter = null) =>
            {
                if (file == null || file.Length == 0)
                    return Results.BadRequest("No image sent");

                var fileExtension = Path.GetExtension(file.FileName.ToLower());

                if (!allowedExtensions.Contains(fileExtension))
                    return Results.BadRequest("Unsupported file type");

                try
                {
                    var output = await FilterImageAsync(file, filter);

                    string s3Key = $"processed-images/{Guid.NewGuid()}.jpeg";

                    var putRequest = new PutObjectRequest()
                    {
                        BucketName = bucketName,
                        Key = s3Key,
                        InputStream = output,
                        ContentType = "image/jpeg"
                    };

                    await s3Client.PutObjectAsync(putRequest);

                    string fileUrl = $"http://{bucketName}.s3.{bucketRegion.SystemName}.amazonaws.com/{s3Key}";

                    return Results.Ok(fileUrl);
                } 
                catch (ArgumentException)
                {
                    return Results.BadRequest("Invalid filter");
                }
                catch (MagickMissingDelegateErrorException)
                {
                    return Results.BadRequest("File extension does not match contents");
                }
                catch (MagickCorruptImageErrorException)
                {
                    return Results.BadRequest("Image contents corrupt or invalid");
                }
                catch (AmazonS3Exception e)
                {
                    return Results.Problem(title: "AWS S3 Storage error", detail: e.Message, statusCode: 500);
                }
                catch (Exception e)
                {
                    return Results.Problem(title: "Unexpected error during file processing", detail: e.Message, statusCode: 500);
                }
            }) .DisableAntiforgery();

            app.Run();
        }

        static async Task<MemoryStream> FilterImageAsync(IFormFile file, string filter)
        {
            await using var input = file.OpenReadStream();
            using var image = new MagickImage(input);

            switch(filter)
            {
                case "none":
                    break;

                case "grayscale":
                    image.ColorSpace = ColorSpace.Gray;
                    break;

                case "sepia":
                    image.SepiaTone();
                    break;

                case "inverted":
                    image.Negate();
                    break;

                case "blur":
                    image.Blur(0, 3);
                    break;

                case "pixelated":
                    image.FilterType = FilterType.Point;
                    image.Resize(new Percentage(20));
                    image.Resize(new Percentage(500));
                    break;

                default:
                    throw new ArgumentException();
            }

            var output = new MemoryStream();
            await image.WriteAsync(output, MagickFormat.Jpeg);
            output.Position = 0;

            return output;
        }
    }
}
