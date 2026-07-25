cd ../DDSCreator
dotnet publish DDSCreator.csproj /p:PublishProfile="win-x86"
dotnet publish DDSCreator.csproj /p:PublishProfile="win-x64"
dotnet publish DDSCreator.csproj /p:PublishProfile="linux-64"
dotnet publish DDSCreator.csproj /p:PublishProfile="osx-64"
pause