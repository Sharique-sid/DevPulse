while ($true) {
    $jobs = 1..5 | ForEach-Object {
        Start-Job -ScriptBlock {
            try { Invoke-WebRequest -Uri "https://devpulse-api-260505673608.us-central1.run.app/health" -UseBasicParsing -TimeoutSec 5 | Out-Null } catch {}
            try { Invoke-WebRequest -Uri "https://devpulse-app-260505673608.us-central1.run.app" -UseBasicParsing -TimeoutSec 5 | Out-Null } catch {}
        }
    }
    $jobs | Wait-Job | Remove-Job
    Write-Host "[$((Get-Date).ToString('HH:mm:ss'))] Batch done"
}
