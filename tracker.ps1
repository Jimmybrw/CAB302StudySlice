$currentDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$logFile = Join-Path $currentDir "active.txt"

# This snippet uses a Windows API call to find the ACTUAL foreground window
$code = @'
    [DllImport("user32.dll")]
    public static extern IntPtr GetForegroundWindow();
    [DllImport("user32.dll")]
    public static extern int GetWindowText(IntPtr hWnd, System.Text.StringBuilder lpString, int nMaxCount);
'@
$winApi = Add-Type -MemberDefinition $code -Name "Win32GetForegroundWindow" -Namespace Win32Functions -PassThru

Do {
    $hwnd = $winApi::GetForegroundWindow()
    $sb = New-Object System.Text.StringBuilder 256
    $null = $winApi::GetWindowText($hwnd, $sb, 256)
    $title = $sb.ToString()

    if (-not $title) { $title = "Desktop" }
    
    # Write to file for Java Engine to read
    $title | Out-File -FilePath $logFile -Encoding ascii
    Start-Sleep -Seconds 1
} While ($true)