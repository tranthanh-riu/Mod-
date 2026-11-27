# Builds the Elden Ring Core modpack as a Modrinth .mrpack file.
#
#   1. .\gradlew.bat build --no-daemon      (makes build/libs/eldenring_core-*.jar)
#   2. powershell -ExecutionPolicy Bypass -File modpack\build-mrpack.ps1
#
# Output: modpack/dist/EldenRingCore-<version>.mrpack
# Import it with the Modrinth App or Prism Launcher.
# Every mod is downloaded from its official Modrinth page using the pinned version and
# hash in modpack/mods.json; only our own jar and the overrides/ folder ship in the file.

$ErrorActionPreference = 'Stop'
$root    = Split-Path -Parent $PSScriptRoot
$packDir = $PSScriptRoot
$spec    = Get-Content (Join-Path $packDir 'mods.json') -Raw -Encoding UTF8 | ConvertFrom-Json

$ourJar = Get-ChildItem (Join-Path $root 'build\libs') -Filter 'eldenring_core-*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch '(sources|javadoc|slim)' } |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $ourJar) { throw 'build/libs/eldenring_core-*.jar not found. Run .\gradlew.bat build first.' }

$files = foreach ($m in $spec.mods) {
    [ordered]@{
        path      = "mods/$($m.file)"
        hashes    = [ordered]@{ sha1 = $m.sha1; sha512 = $m.sha512 }
        env       = [ordered]@{ client = $m.client; server = $m.server }
        downloads = @($m.url)
        fileSize  = [int64]$m.size
    }
}

$index = [ordered]@{
    formatVersion = 1
    game          = 'minecraft'
    versionId     = $spec.version
    name          = $spec.name
    summary       = 'Elden Ring bosses, weapons and the 7-region Lands Between on Epic Fight.'
    files         = @($files)
    dependencies  = [ordered]@{ minecraft = $spec.minecraft; forge = $spec.forge }
}

$stage = Join-Path ([IO.Path]::GetTempPath()) ("mrpack-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $stage | Out-Null
try {
    $utf8 = New-Object Text.UTF8Encoding $false
    [IO.File]::WriteAllText((Join-Path $stage 'modrinth.index.json'), ($index | ConvertTo-Json -Depth 6), $utf8)
    Copy-Item (Join-Path $packDir 'overrides') -Destination (Join-Path $stage 'overrides') -Recurse
    $modsOut = Join-Path $stage 'overrides\mods'
    New-Item -ItemType Directory -Force -Path $modsOut | Out-Null
    Copy-Item $ourJar.FullName -Destination $modsOut

    $dist = Join-Path $packDir 'dist'
    New-Item -ItemType Directory -Force -Path $dist | Out-Null
    $out = Join-Path $dist ("EldenRingCore-{0}.mrpack" -f $spec.version)
    if (Test-Path $out) { Remove-Item $out -Force -Confirm:$false }

    # Zip with forward-slash entry names (Compress-Archive on PowerShell 5 writes backslashes).
    Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::Open($out, 'Create')
    try {
        Get-ChildItem $stage -Recurse -File | ForEach-Object {
            $rel = $_.FullName.Substring($stage.Length + 1).Replace('\', '/')
            [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $rel) | Out-Null
        }
    } finally { $zip.Dispose() }

    Write-Host ("Built {0} ({1} mods + {2})" -f $out, @($spec.mods).Count, $ourJar.Name)
} finally {
    Remove-Item $stage -Recurse -Force -Confirm:$false
}
