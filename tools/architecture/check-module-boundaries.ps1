$ErrorActionPreference = "Stop"

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$corePom = Join-Path $root "practico-core-service/pom.xml"
$authPom = Join-Path $root "practico-auth-service/pom.xml"
$coreSources = Join-Path $root "practico-core-service/src/main/java"

if (!(Test-Path $corePom) -or !(Test-Path $authPom)) {
    throw "Expected module poms were not found."
}

$corePomText = Get-Content $corePom -Raw
$authPomText = Get-Content $authPom -Raw

if ($corePomText -match "<artifactId>\s*practico-auth-service\s*</artifactId>") {
    throw "Forbidden dependency: core -> auth-service"
}

if ($authPomText -match "<artifactId>\s*practico-core-service\s*</artifactId>") {
    throw "Forbidden dependency: auth-service -> core-service"
}

$forbiddenPatterns = @(
    "TelegramAuthenticationProvider",
    "GoogleAuthenticationProvider",
    "RefreshSession",
    "IdentityRepository",
    "/api/auth/login",
    "/api/auth/refresh",
    "/api/auth/sessions"
)

$violations = @()
Get-ChildItem -Path $coreSources -Recurse -Filter *.java | ForEach-Object {
    $text = Get-Content $_.FullName -Raw
    foreach ($pattern in $forbiddenPatterns) {
        if ($text -match [regex]::Escape($pattern)) {
            $violations += "$($_.FullName): contains forbidden pattern '$pattern'"
        }
    }
}

if ($violations.Count -gt 0) {
    $violations | ForEach-Object { Write-Error $_ }
    throw "Module boundary check failed."
}

Write-Output "Module boundary check passed."
