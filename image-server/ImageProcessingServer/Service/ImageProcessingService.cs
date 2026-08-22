using ImageMagick;

namespace ImageProcessingServer.Service
{
    public class ImageProcessingService : IImageProcessingService
    {
        private static readonly string[] AllowedExtensions = [".jpg", ".jpeg", ".png"];
        private readonly IImageStorage _imageStorage;

        public ImageProcessingService(IImageStorage imageStorage)
        {
            _imageStorage = imageStorage;
        }

        public async Task<string> ProcessImageAsync(IFormFile file, string filter)
        {
            if (file == null || file.Length == 0)
                throw new ArgumentException("No image sent");

            var fileExtension = Path.GetExtension(file.FileName.ToLowerInvariant());

            if (!AllowedExtensions.Contains(fileExtension))
                throw new ArgumentException("Unsupported file type");

            await using var output = await FilterImageAsync(file, filter);
            return await _imageStorage.UploadProcessedImageAsync(output);
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
                    image.SepiaTone(new Percentage(80));
                    break;
                case "inverted":
                    image.Negate();
                    break;
                case "blur":
                    image.Blur(0, 3);
                    break;
                case "pixelated":
                    uint originalWidth = image.Width;
                    uint originalHeight = image.Height;
                    image.FilterType = FilterType.Point;
                    image.Resize(
                        Math.Max(1u, originalWidth / 5),
                        Math.Max(1u, originalHeight / 5)
                    );
                    image.Resize(new MagickGeometry(originalWidth, originalHeight)
                    {
                        IgnoreAspectRatio = true
                    });
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
