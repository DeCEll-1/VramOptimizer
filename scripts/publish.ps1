param (
    [Parameter(Mandatory = $true)]
    [string]$TagName
)

Write-Host "Building release for tag: $TagName"

Set-Location "../DDSCreator"

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

    $targetSubfolderName = "VramOptimizer"
    $newSubfolderPath = Join-Path $profileDir $targetSubfolderName
    New-Item -Path $newSubfolderPath -ItemType Directory -Force | Out-Null

    Get-ChildItem -LiteralPath $profileDir | Where-Object { $_.Name -ne $targetSubfolderName } | Move-Item -Destination $newSubfolderPath
}

Write-Host "All publishes completed successfully!" -ForegroundColor Green

Write-Host "Compressing profile folders into zip archives..." -ForegroundColor Cyan
foreach ($p in $profiles) {
    $folderPath = "../bin/Publish/$p"
    $zipPath = "../bin/Publish/$p.zip"

    if (Test-Path -LiteralPath $folderPath) {
        if (Test-Path -LiteralPath $zipPath) {
            Remove-Item -LiteralPath $zipPath -Force
        }
        
        Write-Host "Zipping $p to $zipPath" -ForegroundColor Yellow
        Compress-Archive -LiteralPath $folderPath -DestinationPath $zipPath -CompressionLevel Optimal
    }
}

Write-Host "All folders zipped successfully!" -ForegroundColor Green
Pause