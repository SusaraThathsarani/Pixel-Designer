param([string]$BaseUrl = "http://localhost:8080")

$here      = Split-Path -Parent $MyInvocation.MyCommand.Path
$jmx       = Join-Path $here "pixology-health.jmx"
$resultsDir= Join-Path $here "results"
$reportDir = Join-Path $here "report"
$userProps = Join-Path $here "user.properties"
$resultsCsv= Join-Path $resultsDir "results.jtl"

if (Test-Path $resultsDir) { Remove-Item $resultsDir -Recurse -Force }
if (Test-Path $reportDir)  { Remove-Item $reportDir  -Recurse -Force }
New-Item -ItemType Directory -Path $resultsDir,$reportDir | Out-Null

& jmeter -n -t $jmx -JbaseUrl=$BaseUrl -l $resultsCsv -q $userProps -f
if ($LASTEXITCODE -ne 0) { throw "JMeter test run failed" }

& jmeter -g $resultsCsv -o $reportDir -f
if ($LASTEXITCODE -ne 0) { throw "Report generation failed" }

Write-Host "Report: $reportDir\index.html"
