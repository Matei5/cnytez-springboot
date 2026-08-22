using ImageMagick;
using ImageMagick.Drawing;
using ImageProcessingServer.Service;
using Microsoft.AspNetCore.Http;

namespace ImageProcessingServer.Tests;

public class ImageProcessingServiceTests
{
    private const string ExpectedUrl = "https://example.test/processed.jpeg";

    [Theory]
    [InlineData("none")]
    [InlineData("grayscale")]
    [InlineData("sepia")]
    [InlineData("inverted")]
    [InlineData("blur")]
    [InlineData("pixelated")]
    public async Task ProcessImageAsync_ValidImageAndFilter_UploadsJpeg(string filter)
    {
        var storage = new RecordingImageStorage();
        var service = new ImageProcessingService(storage);

        string result = await service.ProcessImageAsync(CreatePngFile(), filter);

        Assert.Equal(ExpectedUrl, result);
        Assert.True(storage.UploadCalled);
        Assert.True(storage.UploadedLength > 0);
    }

    [Fact]
    public async Task ProcessImageAsync_EmptyFile_RejectsRequest()
    {
        var service = new ImageProcessingService(new RecordingImageStorage());
        var file = new FormFile(new MemoryStream(), 0, 0, "file", "empty.png");

        var error = await Assert.ThrowsAsync<ArgumentException>(
            () => service.ProcessImageAsync(file, "none")
        );

        Assert.Equal("No image sent", error.Message);
    }

    [Fact]
    public async Task ProcessImageAsync_UnsupportedExtension_RejectsRequest()
    {
        var service = new ImageProcessingService(new RecordingImageStorage());
        var file = new FormFile(new MemoryStream([1, 2, 3]), 0, 3, "file", "image.gif");

        var error = await Assert.ThrowsAsync<ArgumentException>(
            () => service.ProcessImageAsync(file, "none")
        );

        Assert.Equal("Unsupported file type", error.Message);
    }

    [Fact]
    public async Task ProcessImageAsync_UnknownFilter_RejectsRequestWithoutUploading()
    {
        var storage = new RecordingImageStorage();
        var service = new ImageProcessingService(storage);

        var error = await Assert.ThrowsAsync<ArgumentException>(
            () => service.ProcessImageAsync(CreatePngFile(), "unknown")
        );

        Assert.Equal("Invalid filter", error.Message);
        Assert.False(storage.UploadCalled);
    }

    [Theory]
    [InlineData("none")]
    [InlineData("grayscale")]
    [InlineData("sepia")]
    [InlineData("inverted")]
    [InlineData("blur")]
    [InlineData("pixelated")]
    public async Task ProcessImageAsync_EveryFilter_PreservesOriginalDimensions(string filter)
    {
        var storage = new RecordingImageStorage();
        var service = new ImageProcessingService(storage);

        await service.ProcessImageAsync(CreatePatternFile(13, 17), filter);

        using var output = new MagickImage(storage.UploadedBytes!);
        Assert.Equal(13u, output.Width);
        Assert.Equal(17u, output.Height);
        Assert.Equal(MagickFormat.Jpeg, output.Format);
    }

    [Fact]
    public async Task ProcessImageAsync_Grayscale_ProducesNeutralPixels()
    {
        var output = await ProcessSolidColor(MagickColors.Red, "grayscale");
        var color = output.GetPixels().GetPixel(4, 4).ToColor()!;

        Assert.InRange(Math.Abs((int)color.R - color.G), 0, 2);
        Assert.InRange(Math.Abs((int)color.G - color.B), 0, 2);
    }

    [Fact]
    public async Task ProcessImageAsync_Inverted_ProducesComplementaryColor()
    {
        var output = await ProcessSolidColor(MagickColors.Red, "inverted");
        var color = output.GetPixels().GetPixel(4, 4).ToColor()!;

        Assert.True(color.R < color.G);
        Assert.True(color.R < color.B);
    }

    [Fact]
    public async Task ProcessImageAsync_Sepia_ProducesWarmOrderedChannels()
    {
        var output = await ProcessSolidColor(MagickColors.Blue, "sepia");
        var color = output.GetPixels().GetPixel(4, 4).ToColor()!;

        Assert.True(
            color.R >= color.G && color.G >= color.B && color.R > color.B,
            $"Expected warm sepia channels but got R={color.R}, G={color.G}, B={color.B}"
        );
    }

    [Fact]
    public async Task ProcessImageAsync_Blur_SoftensASharpEdge()
    {
        using var source = new MagickImage(MagickColors.Black, 21, 9);
        source.Draw(new Drawables().FillColor(MagickColors.White).Rectangle(10, 0, 20, 8));
        var output = await ProcessImage(CreateFile(source), "blur");
        var pixels = output.GetPixels();
        var nearBlackEdge = pixels.GetPixel(8, 4).ToColor()!;
        var nearWhiteEdge = pixels.GetPixel(12, 4).ToColor()!;

        Assert.True(nearBlackEdge.R > 0);
        Assert.True(nearWhiteEdge.R < ushort.MaxValue);
    }

    [Fact]
    public async Task ProcessImageAsync_Pixelated_ChangesDetailedPattern()
    {
        using var source = CreateCheckerboard(25, 25);
        using var unchanged = await ProcessImage(CreateFile(source), "none");
        using var pixelated = await ProcessImage(CreateFile(source), "pixelated");

        double difference = unchanged.Compare(pixelated, ErrorMetric.RootMeanSquared);
        Assert.True(difference > 0.05);
    }

    [Fact]
    public async Task ProcessImageAsync_InvalidImageBytes_RejectsBeforeUpload()
    {
        var storage = new RecordingImageStorage();
        var service = new ImageProcessingService(storage);
        byte[] invalid = "not an image"u8.ToArray();
        var file = new FormFile(new MemoryStream(invalid), 0, invalid.Length, "file", "fake.png");

        await Assert.ThrowsAnyAsync<MagickException>(
            () => service.ProcessImageAsync(file, "none")
        );
        Assert.False(storage.UploadCalled);
    }

    private static IFormFile CreatePngFile()
    {
        byte[] png = Convert.FromBase64String(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
        );
        return new FormFile(new MemoryStream(png), 0, png.Length, "file", "image.png");
    }

    private static IFormFile CreatePatternFile(uint width, uint height)
    {
        using var image = CreateCheckerboard(width, height);
        return CreateFile(image);
    }

    private static MagickImage CreateCheckerboard(uint width, uint height)
    {
        var image = new MagickImage(MagickColors.Black, width, height);
        var drawables = new Drawables().FillColor(MagickColors.White);
        for (uint y = 0; y < height; y += 2)
        {
            for (uint x = y % 4 == 0 ? 0u : 1u; x < width; x += 2)
                drawables.Rectangle(x, y, x, y);
        }
        image.Draw(drawables);
        return image;
    }

    private static IFormFile CreateFile(MagickImage image)
    {
        var stream = new MemoryStream();
        image.Write(stream, MagickFormat.Png);
        stream.Position = 0;
        return new FormFile(stream, 0, stream.Length, "file", "image.png");
    }

    private static async Task<MagickImage> ProcessSolidColor(MagickColor color, string filter)
    {
        using var source = new MagickImage(color, 9, 9);
        return await ProcessImage(CreateFile(source), filter);
    }

    private static async Task<MagickImage> ProcessImage(IFormFile file, string filter)
    {
        var storage = new RecordingImageStorage();
        var service = new ImageProcessingService(storage);
        await service.ProcessImageAsync(file, filter);
        return new MagickImage(storage.UploadedBytes!);
    }

    private sealed class RecordingImageStorage : IImageStorage
    {
        public bool UploadCalled { get; private set; }
        public long UploadedLength { get; private set; }
        public byte[]? UploadedBytes { get; private set; }

        public async Task<string> UploadProcessedImageAsync(Stream image)
        {
            UploadCalled = true;
            UploadedLength = image.Length;
            using var copy = new MemoryStream();
            await image.CopyToAsync(copy);
            UploadedBytes = copy.ToArray();
            return ExpectedUrl;
        }
    }
}
