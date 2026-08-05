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

        Add-Type -AssemblyName System.IO.Compression.FileSystem
        
        # 1. Create the archive normally
        [System.IO.Compression.ZipFile]::CreateFromDirectory($folderPath, $zipPath, [System.IO.Compression.CompressionLevel]::Optimal, $false)

        # 2. Open it back up and fix the backslashes to forward slashes
        $fs = [System.IO.File]::Open($zipPath, [System.IO.FileMode]::Open)
        $zip = New-Object System.IO.Compression.ZipArchive($fs, [System.IO.Compression.ZipArchiveMode]::Update)

        # PowerShell 5.1/Windows .NET workaround: rewrite internal paths
        foreach ($entry in $zip.Entries) {
            # If the internal name contains a backslash, replace it
            if ($entry.FullName.Contains('\')) {
                # Reflection is required to modify FullName directly as it's read-only
                $property = $entry.GetType().GetProperty("FullName", [System.Reflection.BindingFlags]"Instance,Public,NonPublic")
                $property.SetValue($entry, $entry.FullName.Replace('\', '/'), $null)
            }
        }

        $zip.Dispose()
        $fs.Dispose()
    }
}

Write-Host "All folders zipped successfully!" -ForegroundColor Green
Pause