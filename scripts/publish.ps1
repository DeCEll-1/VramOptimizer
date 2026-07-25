Set-Location "../DDSCreator"

$profiles = @("win-x86", "win-x64", "linux-x64", "osx-x64")

if (!(Test-Path -LiteralPath "../bin/Publish")) {
    New-Item -Path "../bin/" -Name "Publish" -ItemType Directory
}

foreach ($p in $profiles) {
    Write-Host "Publishing profile: $p..." -ForegroundColor Cyan
    $profileDir = "../bin/Publish/$p"
    if (Test-Path -LiteralPath $profileDir) {
        Remove-Item -LiteralPath $profileDir -Recurse -Force
    }
    dotnet publish DDSCreator.csproj /p:PublishProfile="$p"
    $binDir = "$profileDir/bin"
    if (!(Test-Path -LiteralPath $binDir)) {
        New-Item -Path "$profileDir" -Name "bin" -ItemType Directory | Out-Null
    }
    Move-Item -Path "$profileDir/DDSCreator*", "$profileDir/Magick.Native*" -Destination $binDir
}

Write-Host "All publishes completed successfully!" -ForegroundColor Green
Pause