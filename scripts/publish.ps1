param (
    [Parameter(Mandatory = $true)]
    [string]$TagName
)

Write-Host "Building release for tag: $TagName"

& "$PSScriptRoot/build.ps1" $TagName

Set-Location "../DDSCreator"

$profiles = @("VramOptimizer-win-x64", "VramOptimizer-win-x86", "VramOptimizer-linux-x64", "VramOptimizer-osx-x64",
    "VramOptimizer-win-x64-sc", "VramOptimizer-win-x86-sc", "VramOptimizer-linux-x64-sc", "VramOptimizer-osx-x64-sc"
)


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