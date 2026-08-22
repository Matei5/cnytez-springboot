using Amazon.S3;
using Amazon.S3.Model;
using ImageProcessingServer.Service;
using Moq;

namespace ImageProcessingServer.Tests;

public class S3ImageStorageTests
{
    [Fact]
    public async Task UploadProcessedImageAsync_BuildsExpectedS3RequestAndHttpsUrl()
    {
        PutObjectRequest? captured = null;
        var s3 = new Mock<IAmazonS3>(MockBehavior.Strict);
        s3.Setup(client => client.PutObjectAsync(
                It.IsAny<PutObjectRequest>(),
                It.IsAny<CancellationToken>()))
            .Callback<PutObjectRequest, CancellationToken>((request, _) => captured = request)
            .ReturnsAsync(new PutObjectResponse());
        var storage = new S3ImageStorage(s3.Object);
        using var image = new MemoryStream([1, 2, 3]);

        string url = await storage.UploadProcessedImageAsync(image);

        Assert.NotNull(captured);
        Assert.Equal("cnytez-image-server-s3", captured.BucketName);
        Assert.StartsWith("processed-images/", captured.Key);
        Assert.EndsWith(".jpeg", captured.Key);
        Assert.Equal("image/jpeg", captured.ContentType);
        Assert.Same(image, captured.InputStream);
        Assert.Equal(
            $"https://cnytez-image-server-s3.s3.eu-central-1.amazonaws.com/{captured.Key}",
            url
        );
        s3.VerifyAll();
    }

    [Fact]
    public async Task UploadProcessedImageAsync_TwoUploadsUseDifferentKeys()
    {
        var keys = new List<string>();
        var s3 = new Mock<IAmazonS3>();
        s3.Setup(client => client.PutObjectAsync(
                It.IsAny<PutObjectRequest>(),
                It.IsAny<CancellationToken>()))
            .Callback<PutObjectRequest, CancellationToken>((request, _) => keys.Add(request.Key))
            .ReturnsAsync(new PutObjectResponse());
        var storage = new S3ImageStorage(s3.Object);

        await storage.UploadProcessedImageAsync(new MemoryStream([1]));
        await storage.UploadProcessedImageAsync(new MemoryStream([2]));

        Assert.Equal(2, keys.Distinct().Count());
    }
}
