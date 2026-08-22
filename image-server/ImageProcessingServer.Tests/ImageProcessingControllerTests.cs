using Amazon.S3;
using ImageProcessingServer.Controller;
using ImageProcessingServer.Service;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace ImageProcessingServer.Tests;

public class ImageProcessingControllerTests
{
    [Fact]
    public async Task ProcessImage_ServiceSucceeds_ReturnsUrl()
    {
        const string expectedUrl = "https://example.test/image.jpeg";
        var controller = new ImageProcessingController(
            new StubImageProcessingService(expectedUrl)
        );

        IActionResult result = await controller.ProcessImage(CreateFile(), "none");

        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.Equal(expectedUrl, ok.Value);
    }

    [Fact]
    public async Task ProcessImage_InvalidInput_ReturnsBadRequest()
    {
        var controller = new ImageProcessingController(
            new StubImageProcessingService(new ArgumentException("Invalid filter"))
        );

        IActionResult result = await controller.ProcessImage(CreateFile(), "unknown");

        var badRequest = Assert.IsType<BadRequestObjectResult>(result);
        Assert.Equal("Invalid filter", badRequest.Value);
    }

    [Fact]
    public async Task ProcessImage_S3Failure_ReturnsServerError()
    {
        var controller = new ImageProcessingController(
            new StubImageProcessingService(new AmazonS3Exception("S3 unavailable"))
        );

        IActionResult result = await controller.ProcessImage(CreateFile(), "none");

        var problem = Assert.IsType<ObjectResult>(result);
        Assert.Equal(StatusCodes.Status500InternalServerError, problem.StatusCode);
        Assert.Equal("AWS S3 Storage error", Assert.IsType<ProblemDetails>(problem.Value).Title);
    }

    [Fact]
    public void HealthCheck_ReturnsOk()
    {
        var controller = new ImageProcessingController(
            new StubImageProcessingService("unused")
        );

        var result = Assert.IsType<OkObjectResult>(controller.HealthCheck());

        Assert.Equal("Image processing server is available", result.Value);
    }

    private static IFormFile CreateFile()
    {
        return new FormFile(new MemoryStream([1]), 0, 1, "file", "image.png");
    }

    private sealed class StubImageProcessingService : IImageProcessingService
    {
        private readonly string? _result;
        private readonly Exception? _exception;

        public StubImageProcessingService(string result)
        {
            _result = result;
        }

        public StubImageProcessingService(Exception exception)
        {
            _exception = exception;
        }

        public Task<string> ProcessImageAsync(IFormFile file, string filter)
        {
            if (_exception is not null)
                return Task.FromException<string>(_exception);

            return Task.FromResult(_result!);
        }
    }
}
