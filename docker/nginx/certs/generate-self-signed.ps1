param(
    [string]$Domain = "localhost",
    [int]$Days = 365
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$keyPath = Join-Path $scriptDir "privkey.pem"
$certPath = Join-Path $scriptDir "fullchain.pem"

openssl req -x509 -nodes -newkey rsa:2048 `
  -keyout $keyPath `
  -out $certPath `
  -days $Days `
  -subj "/CN=$Domain"

Write-Host "Generated:"
Write-Host "  $certPath"
Write-Host "  $keyPath"
