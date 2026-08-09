param (
    [Parameter(Mandatory = $true)]
    [string]$TagName
)

& "./buildSharpShaders.bat"
Set-Location "../DDSCreator"
# this exists to check if everything builds correctly
$profiles = @("VramOptimizer-win-x64", "VramOptimizer-win-x86", "VramOptimizer-linux-x64",
    "VramOptimizer-win-x64-sc", "VramOptimizer-win-x86-sc", "VramOptimizer-linux-x64-sc"
)

if (!(Test-Path -LiteralPath "../bin/Publish")) {
    New-Item -Path "../bin/" -Name "Publish" -ItemType Directory
}
$gistId = "60499ce4f3e5d26aa2d62c849c19c875"
$numbers = ($TagName -replace "[a-zA-Z]", "").Split(".")
$major = $numbers[0]
$minor = $numbers[1]
$patch = $numbers[2]

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


    $fileName = "$p.version"
    
    # Generate the file content for this profile
    $fileContent = @{
        masterVersionFile = "https://gist.githubusercontent.com/DeCEll-1/$gistId/raw/$fileName"
        directDownloadURL = "https://github.com/DeCEll-1/VramOptimizer/releases/latest/download/$p.zip"
        modName           = "~Vram Optimizer"
        modThreadId       = 35788
        modVersion        = @{
            major = [int]$major
            minor = [int]$minor
            patch = [int]$patch
        }
    } | ConvertTo-Json -Compress
    
    Set-Content -LiteralPath "$profileDir/VOpt.version" -Value $fileContent

    #update the version
    Set-Content -LiteralPath "$profileDir/VOpt_VERSION.txt" -Value "VOpt $TagName"

    if ($profileDir.Contains("win")) {
        Remove-Item -LiteralPath "$profileDir/External/ispc_texcomp/ispc_texcomp_linux" -Recurse -Force
    } elseif ($profileDir.Contains("linux")) {
        Remove-Item -LiteralPath "$profileDir/External/ispc_texcomp/ispc_texcomp_win" -Recurse -Force
    }

    $targetSubfolderName = "VramOptimizer"
    $newSubfolderPath = Join-Path $profileDir $targetSubfolderName
    New-Item -Path $newSubfolderPath -ItemType Directory -Force | Out-Null

    Get-ChildItem -LiteralPath $profileDir | Where-Object { $_.Name -ne $targetSubfolderName } | Move-Item -Destination $newSubfolderPath
}

Write-Host "All builds completed." -ForegroundColor Green