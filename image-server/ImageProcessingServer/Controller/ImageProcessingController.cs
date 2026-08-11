using Amazon.S3;
using ImageMagick;
using ImageProcessingServer.Service;
using Microsoft.AspNetCore.Mvc;

namespace ImageProcessingServer.Controller
{
    [ApiController]
    public class ImageProcessingController: ControllerBase
    {
        private readonly ImageProcessingService _imageProcessingService;

        public ImageProcessingController(ImageProcessingService imageProcessingService)
        {
            _imageProcessingService = imageProcessingService;
        }

        [HttpPost("/{filter}")]
        public async Task<IActionResult> ProcessImage([FromForm] IFormFile file, string filter)
        {
            try
            {
                string fileUrl = await _imageProcessingService.ProcessImageAsync(file, filter);

                return Ok(fileUrl);
            }
            catch (ArgumentException e)
            {
                return BadRequest(e.Message);
            }
            catch (MagickMissingDelegateErrorException)
            {
                return BadRequest("File extension does not match contents");
            }
            catch (MagickCorruptImageErrorException)
            {
                return BadRequest("Image contents corrupt or invalid");
            }
            catch (AmazonS3Exception e)
            {
                return Problem(title: "AWS S3 Storage error", detail: e.Message, statusCode: 500);
            }
            catch (Exception e)
            {
                return Problem(title: "Unexpected error during file processing", detail: e.Message, statusCode: 500);
            }
        }

        [HttpGet("/")]
        public IActionResult HealthCheck()
        {
            return Ok("Image processing server is available");
        }
    }
}