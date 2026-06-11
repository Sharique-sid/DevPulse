while ($true) {
    $jobs = 1..5 | ForEach-Object {
        Start-Job -ScriptBlock {
            try { Invoke-WebRequest -Uri "https://devpulse-tgrl.onrender.com/health" -UseBasicParsing -TimeoutSec 5 | Out-Null } catch {}
            try { Invoke-WebRequest -Uri "https://dev-pulse-rust.vercel.app/" -UseBasicParsing -TimeoutSec 5 | Out-Null } catch {}
        }
    }
    $jobs | Wait-Job | Remove-Job
    Write-Host "[$((Get-Date).ToString('HH:mm:ss'))] Batch done"
}
