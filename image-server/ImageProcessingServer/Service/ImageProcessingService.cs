using Amazon;
using Amazon.S3;
using Amazon.S3.Model;
using ImageMagick;

namespace ImageProcessingServer.Service
{
    public class ImageProcessingService
    {
        private const string bucketName = "cnytez-image-server-s3";
        private static readonly RegionEndpoint bucketRegion = RegionEndpoint.EUCentral1;
        private static AmazonS3Client s3Client = new AmazonS3Client(bucketRegion);
        private static string[] allowedExtensions = [".jpg", ".jpeg", ".png"];

        public async Task<string> ProcessImageAsync(IFormFile file, string filter)
        {
            if (file == null || file.Length == 0)
                throw new ArgumentException("No image sent");

            var fileExtension = Path.GetExtension(file.FileName.ToLower());

            if (!allowedExtensions.Contains(fileExtension))
                throw new ArgumentException("Unsupported file type");

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

            return fileUrl;
        }

        private static async Task<MemoryStream> FilterImageAsync(IFormFile file, string filter)
        {
            await using var input = file.OpenReadStream();
            using var image = new MagickImage(input);

            switch (filter)
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
                    throw new ArgumentException("Invalid filter");
            }

            var output = new MemoryStream();
            await image.WriteAsync(output, MagickFormat.Jpeg);
            output.Position = 0;

            return output;
        }
    }
}
