cd ..
CALL ./scripts/buildExternal.bat
dotnet build --configuration Release ./DDSCreator
pause