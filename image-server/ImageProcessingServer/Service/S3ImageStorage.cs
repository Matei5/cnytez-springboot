using Amazon;
using Amazon.S3;
using Amazon.S3.Model;

namespace ImageProcessingServer.Service
{
    public class S3ImageStorage : IImageStorage
    {
        private const string BucketName = "cnytez-image-server-s3";
        private static readonly RegionEndpoint BucketRegion = RegionEndpoint.EUCentral1;
        private readonly IAmazonS3 _s3Client;

        public S3ImageStorage(IAmazonS3 s3Client)
        {
            _s3Client = s3Client;
        }

        public async Task<string> UploadProcessedImageAsync(Stream image)
        {
            string s3Key = $"processed-images/{Guid.NewGuid()}.jpeg";
            var putRequest = new PutObjectRequest
            {
                BucketName = BucketName,
                Key = s3Key,
                InputStream = image,
                ContentType = "image/jpeg"
            };

            await _s3Client.PutObjectAsync(putRequest);

            return $"https://{BucketName}.s3.{BucketRegion.SystemName}.amazonaws.com/{s3Key}";
        }
    }
}
