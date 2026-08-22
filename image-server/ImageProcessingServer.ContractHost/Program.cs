using ImageProcessingServer.Controller;
using ImageProcessingServer.Service;

var builder = WebApplication.CreateBuilder(args);

builder.Services
    .AddControllers()
    .AddApplicationPart(typeof(ImageProcessingController).Assembly);
builder.Services.AddHealthChecks();
builder.Services.AddScoped<IImageStorage, ContractImageStorage>();
builder.Services.AddScoped<IImageProcessingService, ImageProcessingService>();

var app = builder.Build();

app.MapControllers();
app.MapHealthChecks("/health/ready");
app.Run();

internal sealed class ContractImageStorage : IImageStorage
{
    public async Task<string> UploadProcessedImageAsync(Stream image)
    {
        using var output = new MemoryStream();
        await image.CopyToAsync(output);

        if (output.Length == 0)
        {
            throw new InvalidOperationException("Processed image must not be empty");
        }

        return "https://example.test/processed-image.jpeg";
    }
}
