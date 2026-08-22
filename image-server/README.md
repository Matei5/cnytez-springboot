## Cnytez Reddit Project - Image Processing Server

### General info
This is the image processing server used in the Reddit project.
It is responsible for uploading images and applying filters.
### Solution structure
- **ImageProcessingServer**: The core ASP.NET Core web microservice (controllers, filter pipeline, S3 storage, and health checks).
- **ImageProcessingServer.Tests**: Comprehensive xUnit test suite (unit tests, image math verification, S3 mocking, and HTTP integration tests).
- **ImageProcessingServer.ContractHost**: Lightweight in-memory runner for running local cross-service round-trip tests without AWS credentials.
- **ImageProcessingServer.ServiceDefaults**: Utilities for OpenTelemetry and service defaults.
- **ImageProcessingServer.AppHost**: Tools for Aspire local development (not used in Docker/EC2 production).
### How to deploy and run
In order to be functional, this code must run on an AWS EC2 instance with the
AmazonS3FullAccess IAM role. AWS S3 credentials used in code must correspond with the bucket
that is intended to be used as storage.
#### Deployment
1. Ensure that you have the valid SSH key (DDO_Cnytez_Image_Key.pem)
2. Use the following commands in PowerShell:
   1. Copy the solution directory into the EC2 instance: scp -i "DDO_Cnytez_Image_Key.pem" -r "cnytez-springboot/image-server" ec2-user@18.193.138.107:/home/ec2-user/
   2. Login into the instance: ssh -i "DDO_Cnytez_Image_Key.pem" ec2-user@ec2-18-193-138-107.eu-central-1.compute.amazonaws.com
   3. Enter the solution directory: cd ~/image-server
   4. Use docker compose to build and run the container: docker compose up -d --build
   5. In order to stop the container: docker compose down
     
   Note: the path to the SSH key and to the image-server directory must either be an absolute path (C/users/...../DDO_Cnytez_Image_Key.pem) or in the same folder where you are located.
3. if you receive a permission error when trying to use the SSH key, restrict the access to it with following commands:
   1. icacls "DDO_Cnytez_Image_Key.pem" /inheritance:r
   2. icacls "DDO_Cnytez_Image_Key.pem" /remove "NT AUTHORITY\Authenticated Users"
   3. icacls "DDO_Cnytez_Image_Key.pem" /remove "BUILTIN\Users"
   4. icacls "DDO_Cnytez_Image_Key.pem" /grant:r "\$($env:USERNAME):(R)"