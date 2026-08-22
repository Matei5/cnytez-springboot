namespace ImageProcessingServer.Service
{
    public interface IImageProcessingService
    {
        Task<string> ProcessImageAsync(IFormFile file, string filter);
    }
}
