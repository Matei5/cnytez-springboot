using System.Net;
using System.Net.Http.Headers;
using ImageMagick;
using ImageProcessingServer.Service;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.TestHost;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;

namespace ImageProcessingServer.Tests;

public class ImageProcessingHttpTests : IClassFixture<ImageProcessingHttpTests.Factory>
{
    private readonly HttpClient _client;

    public ImageProcessingHttpTests(Factory factory)
    {
        _client = factory.CreateClient();
    }

    [Fact]
    public async Task ProcessImage_ValidMultipartRequest_UsesRoutingBindingAndReturnsUrl()
    {
        using var image = new MagickImage(MagickColors.Red, 5, 5);
        using var bytes = new MemoryStream();
        image.Write(bytes, MagickFormat.Png);
        using var request = CreateMultipart(bytes.ToArray(), "image.png");

        using var response = await _client.PostAsync("/grayscale", request);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal("https://example.test/http.jpeg", await response.Content.ReadAsStringAsync());
    }

    [Fact]
    public async Task ProcessImage_MalformedImage_ReturnsBadRequestWithoutUploading()
    {
        using var request = CreateMultipart("not an image"u8.ToArray(), "fake.png");

        using var response = await _client.PostAsync("/none", request);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ProcessImage_MissingFile_ReturnsBadRequest()
    {
        using var request = new MultipartFormDataContent();

        using var response = await _client.PostAsync("/none", request);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    private static MultipartFormDataContent CreateMultipart(byte[] bytes, string fileName)
    {
        var request = new MultipartFormDataContent();
        var file = new ByteArrayContent(bytes);
        file.Headers.ContentType = new MediaTypeHeaderValue("image/png");
        request.Add(file, "file", fileName);
        return request;
    }

    public sealed class Factory : WebApplicationFactory<ImageProcessingServer.Program>
    {
        protected override void ConfigureWebHost(IWebHostBuilder builder)
        {
            builder.ConfigureTestServices(services =>
            {
                services.RemoveAll<IImageStorage>();
                services.AddSingleton<IImageStorage, TestImageStorage>();
            });
        }
    }

    private sealed class TestImageStorage : IImageStorage
    {
        public Task<string> UploadProcessedImageAsync(Stream image) =>
            Task.FromResult("https://example.test/http.jpeg");
    }
}
