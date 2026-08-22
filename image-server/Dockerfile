FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["ImageProcessingServer/ImageProcessingServer.csproj", "ImageProcessingServer/"]
COPY ["ImageProcessingServer.ServiceDefaults/ImageProcessingServer.ServiceDefaults.csproj", "ImageProcessingServer.ServiceDefaults/"]

RUN dotnet restore "ImageProcessingServer/ImageProcessingServer.csproj"

COPY ImageProcessingServer/ ImageProcessingServer/
COPY ImageProcessingServer.ServiceDefaults/ ImageProcessingServer.ServiceDefaults/

WORKDIR "/src/ImageProcessingServer"
RUN dotnet publish -c Release -o /app/out

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app

COPY --from=build /app/out .

RUN apt-get update && apt-get install -y \
    libgdiplus \
    libc6-dev \
    libfontconfig1 \
    curl \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8123

ENTRYPOINT ["dotnet", "ImageProcessingServer.dll"]
