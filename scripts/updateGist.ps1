param (
    [Parameter(Mandatory = $true)]
    [string]$TagName
)

$profiles = @(
    "VramOptimizer-win-x64", "VramOptimizer-win-x86", "VramOptimizer-linux-x64", "VramOptimizer-osx-x64",
    "VramOptimizer-win-x64-sc", "VramOptimizer-win-x86-sc", "VramOptimizer-linux-x64-sc", "VramOptimizer-osx-x64-sc"
)

# Extract major, minor, patch from $TagName (e.g., v1.2.3 -> 1, 2, 3)
$numbers = ($TagName -replace "[a-zA-Z]", "").Split(".")
$major = $numbers[0]
$minor = $numbers[1]
$patch = $numbers[2]

# Your static Gist ID and Gist API token
$gistId = "60499ce4f3e5d26aa2d62c849c19c875"
$token = $env:GIST_TOKEN # Stored as a GitHub Action secret

# Build a hashtable that will hold all files for the gist payload
$filesPayload = @{}

foreach ($p in $profiles) {
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

    # Map the filename to the content structure required by the GitHub API
    $filesPayload[$fileName] = @{
        content = $fileContent
    }
}

# Construct the final body payload for all files
$body = @{
    files = $filesPayload
} | ConvertTo-Json -Depth 10

# Send the PATCH request to update the Gist
$uri = "https://api.github.com/gists/$gistId"
$headers = @{
    "Authorization"        = "Bearer $token"
    "Accept"               = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
    "User-Agent"           = "PowerShell-GistUpdater"
}

try {
    Write-Host "Updating Gist with all profile versions..."
    Invoke-RestMethod -Uri $uri -Method PATCH -Headers $headers -Body $body -ContentType "application/json" | Out-Null
    Write-Host "Successfully updated all files in Gist!" -ForegroundColor Green
}
catch {
    Write-Error "Failed to update Gist. Response: $_"
    exit 1
}