namespace ImageProcessingServer.Service
{
    public interface IImageStorage
    {
        Task<string> UploadProcessedImageAsync(Stream image);
    }
}
