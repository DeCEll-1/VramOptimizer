param (
    [Parameter(Mandatory = $true)]
    [string]$TagName
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true

& "./buildSharpShaders.bat"
if ($LASTEXITCODE -ne 0) {
    Write-Error "buildSharpShaders.bat failed with exit code $LASTEXITCODE"
    exit 1
}

Set-Location "../DDSCreator"

dotnet clean
dotnet restore

# run the tests here when theyre implemented

Write-Host "All tests completed." -ForegroundColor Green