<#
    Levanta el stack completo de FocusGame para desarrollo local:
    PostgreSQL (Docker) + usuario-service (8001) + productividad-service (8002) + gamificacion-service (8004).
#>

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Resolve-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        return $env:JAVA_HOME
    }

    $vsCodeJdk = Get-ChildItem "$env:USERPROFILE\.vscode\extensions\redhat.java-*\jre\*\bin\java.exe" `
        -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending |
        Select-Object -First 1
    if ($vsCodeJdk) {
        return (Resolve-Path "$($vsCodeJdk.Directory)\..").Path
    }

    $javaCmd = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $binDir = Split-Path -Parent $javaCmd.Source
        return Split-Path -Parent $binDir
    }

    throw "No se encontro un JDK 17+. Instala uno y/o define JAVA_HOME antes de ejecutar este script."
}

function Wait-Postgres {
    param([int]$TimeoutSeconds = 60)

    Write-Host "Esperando a que PostgreSQL este listo..." -ForegroundColor Cyan
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        docker exec focusgame-postgres pg_isready -U postgres *> $null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "PostgreSQL listo." -ForegroundColor Green
            return
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
    throw "PostgreSQL no respondio tras $TimeoutSeconds segundos."
}

function Start-ServiceWindow {
    param(
        [string]$Name,
        [string]$Path,
        [string]$JavaHome
    )

    $stamp = Get-Date -Format "yyyyMMdd-HHmmss-fff"
    $logFile = Join-Path $env:TEMP "focusgame-$Name-$stamp.log"
    $launcher = Join-Path $env:TEMP "focusgame-$Name-$stamp.ps1"
    Write-Host "Iniciando $Name... (log: $logFile)" -ForegroundColor Cyan

    $lines = @(
        "`$env:JAVA_HOME = '$JavaHome'"
        "`$env:Path = '$JavaHome\bin;' + `$env:Path"
        "Set-Location '$Path'"
        ".\mvnw.cmd spring-boot:run 2>&1 | Tee-Object -FilePath '$logFile'"
    )
    Set-Content -Path $launcher -Value $lines -Encoding UTF8

    Start-Process powershell.exe -ArgumentList "-NoExit", "-File", $launcher -WorkingDirectory $Path
}

function Test-ServiceReady {
    param([int]$Port)

    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$Port/v3/api-docs" -UseBasicParsing -TimeoutSec 3
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

function Wait-ServicesReady {
    param(
        [hashtable[]]$Services,
        [int]$TimeoutSeconds = 50
    )

    $pending = $Services | ForEach-Object { @{ Name = $_.Name; Port = $_.Port; Ready = $false } }
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds -and ($pending | Where-Object { -not $_.Ready })) {
        foreach ($svc in $pending | Where-Object { -not $_.Ready }) {
            if (Test-ServiceReady -Port $svc.Port) {
                $svc.Ready = $true
                Write-Host "$($svc.Name) listo (en $elapsed s)." -ForegroundColor Green
            }
        }
        if ($pending | Where-Object { -not $_.Ready }) {
            Start-Sleep -Seconds 3
            $elapsed += 3
        }
    }
    foreach ($svc in $pending | Where-Object { -not $_.Ready }) {
        Write-Host "$($svc.Name) no respondio tras $TimeoutSeconds s. Revisa su ventana de consola." -ForegroundColor Yellow
    }
    return $pending
}

$javaHome = Resolve-JavaHome
Write-Host "Usando JAVA_HOME: $javaHome" -ForegroundColor DarkGray

Write-Host "Levantando PostgreSQL con Docker Compose..." -ForegroundColor Cyan
Push-Location $root
docker compose up -d postgres
Pop-Location

Wait-Postgres

Start-ServiceWindow -Name "usuario-service" -Path (Join-Path $root "usuario-service") -JavaHome $javaHome
Start-ServiceWindow -Name "productividad-service" -Path (Join-Path $root "productividad-service") -JavaHome $javaHome
Start-ServiceWindow -Name "gamificacion-service" -Path (Join-Path $root "gamificacion-service") -JavaHome $javaHome

Write-Host "Esperando a que los servicios respondan (en paralelo)..." -ForegroundColor Cyan
$resultados = Wait-ServicesReady -Services @(
    @{ Name = "usuario-service"; Port = 8001 },
    @{ Name = "productividad-service"; Port = 8002 },
    @{ Name = "gamificacion-service"; Port = 8004 }
)

Write-Host ""
foreach ($svc in $resultados) {
    if ($svc.Ready) {
        Write-Host "$($svc.Name) -> http://localhost:$
        ($svc.Port)/swagger-ui.html" -ForegroundColor Green
    } else {
        Write-Host "$($svc.Name) -> aun no responde, revisa su ventana" -ForegroundColor Yellow
    }
}
