Set-Location "../DDSCreator"
# this exists to check if everything builds correctly
$profiles = @("VramOptimizer-win-x64", "VramOptimizer-win-x86", "VramOptimizer-linux-x64", "VramOptimizer-osx-x64",
    "VramOptimizer-win-x64-sc", "VramOptimizer-win-x86-sc", "VramOptimizer-linux-x64-sc", "VramOptimizer-osx-x64-sc"
)

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
    # since i forget to update the settings.json just copy it
    Copy-Item -Path "$profileDir/data/config/defaultSettings.json" -Destination "$profileDir/data/config/settings.json" -Force

    $targetSubfolderName = "VramOptimizer"
    $newSubfolderPath = Join-Path $profileDir $targetSubfolderName
    New-Item -Path $newSubfolderPath -ItemType Directory -Force | Out-Null

    Get-ChildItem -LiteralPath $profileDir | Where-Object { $_.Name -ne $targetSubfolderName } | Move-Item -Destination $newSubfolderPath
}

Write-Host "All builds completed." -ForegroundColor Green